package com.sbp.auth.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.auth.dto.InvitationCheckResult;
import com.sbp.auth.entity.IdentityProvider;
import com.sbp.auth.entity.Invitation;
import com.sbp.auth.repository.IdpEmailDomainRepository;
import com.sbp.auth.repository.InvitationRepository;
import com.sbp.auth.util.EmailDomainParser;
import com.sbp.common.logging.SbpLogger;

@Service
public class InvitationService {

	private final InvitationRepository invitationRepository;

	private final IdpEmailDomainRepository idpEmailDomainRepository;

	private final SbpLogger sbpLog;

	public InvitationService(InvitationRepository invitationRepository,
			IdpEmailDomainRepository idpEmailDomainRepository, SbpLogger sbpLog) {
		this.invitationRepository = invitationRepository;
		this.idpEmailDomainRepository = idpEmailDomainRepository;
		this.sbpLog = sbpLog;
	}

	@Transactional(readOnly = true)
	public InvitationCheckResult checkInvitation(String email) {
		String normalized = email == null ? "" : email.trim();
		return invitationRepository.findPendingByEmailIgnoreCase(normalized)
				.filter(this::isActive)
				.flatMap(invitation -> resolveIdp(normalized, invitation))
				.map(idp -> toFoundResult(normalized, idp))
				.orElseGet(() -> logNotFound(normalized));
	}

	private Optional<IdentityProvider> resolveIdp(String email, Invitation invitation) {
		if (invitation.getIdentityProvider() != null) {
			return Optional.of(invitation.getIdentityProvider());
		}
		String domain = EmailDomainParser.domainFromEmail(email);
		if (domain.isEmpty()) {
			return Optional.empty();
		}
		return idpEmailDomainRepository.findByEmailDomainIgnoreCase(domain)
				.map(mapping -> mapping.getIdentityProvider());
	}

	private boolean isActive(Invitation invitation) {
		Instant expiresAt = invitation.getExpiresAt();
		return expiresAt == null || expiresAt.isAfter(Instant.now());
	}

	private InvitationCheckResult toFoundResult(String email, IdentityProvider idp) {
		sbpLog.authentication().info(
				"Invitation check: email={}, found=true, idpKey={}, idpName={}, loginUrl={}",
				email, idp.getProviderKey(), idp.getDisplayName(), idp.getLoginUrl());
		sbpLog.application().info("Invitation resolved to IdP {} ({}) for {}",
				idp.getDisplayName(), idp.getProviderKey(), email);
		return InvitationCheckResult.found(email, idp.getProviderKey(), idp.getDisplayName(), idp.getLoginUrl());
	}

	private InvitationCheckResult logNotFound(String email) {
		sbpLog.authentication().info("Invitation check: email={}, found=false", email);
		return InvitationCheckResult.notFound(email);
	}

}
