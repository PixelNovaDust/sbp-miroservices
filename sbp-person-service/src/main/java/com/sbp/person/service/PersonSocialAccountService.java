package com.sbp.person.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.PersonSocialAccountRequest;
import com.sbp.person.dto.PersonSocialAccountResponse;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonSocialAccount;
import com.sbp.person.exception.ResourceNotFoundException;
import com.sbp.person.repository.PersonSocialAccountRepository;

@Service
public class PersonSocialAccountService {

	private final PersonSupportService personSupportService;

	private final PersonSocialAccountRepository socialAccountRepository;

	public PersonSocialAccountService(
			PersonSupportService personSupportService,
			PersonSocialAccountRepository socialAccountRepository) {
		this.personSupportService = personSupportService;
		this.socialAccountRepository = socialAccountRepository;
	}

	@Transactional(readOnly = true)
	public List<PersonSocialAccountResponse> list(UUID personId) {
		personSupportService.requirePerson(personId);
		return socialAccountRepository.findByPerson_IdOrderByUsernameAsc(personId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public PersonSocialAccountResponse create(UUID personId, PersonSocialAccountRequest request) {
		Person person = personSupportService.requirePerson(personId);
		Instant now = Instant.now();
		PersonSocialAccount account = new PersonSocialAccount();
		account.setPerson(person);
		account.setSocialPlatformId(request.socialPlatformId());
		account.setUsername(request.username().trim());
		account.setVerified(request.verified() != null && request.verified());
		account.setAvailable(request.available() == null || request.available());
		account.setFollowersCount(request.followersCount());
		account.setCreatedAt(now);
		account.setUpdatedAt(now);
		account.setCreatedBy(request.actor());
		account.setUpdatedBy(request.actor());
		return toResponse(socialAccountRepository.save(account));
	}

	@Transactional
	public PersonSocialAccountResponse update(UUID personId, UUID accountId, PersonSocialAccountRequest request) {
		personSupportService.requirePerson(personId);
		PersonSocialAccount account = socialAccountRepository.findById(accountId)
				.filter(a -> a.getPerson().getId().equals(personId))
				.orElseThrow(() -> new ResourceNotFoundException("Social account not found: " + accountId));
		account.setSocialPlatformId(request.socialPlatformId());
		account.setUsername(request.username().trim());
		if (request.verified() != null) {
			account.setVerified(request.verified());
		}
		if (request.available() != null) {
			account.setAvailable(request.available());
		}
		account.setFollowersCount(request.followersCount());
		account.setUpdatedBy(request.actor());
		return toResponse(socialAccountRepository.save(account));
	}

	@Transactional
	public void delete(UUID personId, UUID accountId) {
		personSupportService.requirePerson(personId);
		if (!socialAccountRepository.existsById(accountId)) {
			throw new ResourceNotFoundException("Social account not found: " + accountId);
		}
		socialAccountRepository.deleteByPerson_IdAndId(personId, accountId);
	}

	private PersonSocialAccountResponse toResponse(PersonSocialAccount account) {
		return new PersonSocialAccountResponse(
				account.getId(),
				account.getSocialPlatformId(),
				account.getUsername(),
				account.isVerified(),
				account.isAvailable(),
				account.getFollowersCount(),
				account.getLastSyncedAt(),
				account.getCreatedAt(),
				account.getUpdatedAt(),
				account.getVersion());
	}

}
