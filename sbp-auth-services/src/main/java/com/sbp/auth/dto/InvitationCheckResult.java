package com.sbp.auth.dto;

public record InvitationCheckResult(
		boolean invitationFound,
		String message,
		String idpProviderKey,
		String idpDisplayName,
		String idpLoginUrl) {

	public static InvitationCheckResult notFound(String email) {
		return new InvitationCheckResult(false,
				"No pending invitation found for " + email + ".",
				null, null, null);
	}

	public static InvitationCheckResult found(String email, String providerKey, String displayName, String loginUrl) {
		return new InvitationCheckResult(true,
				"Invitation found for " + email + ". Use identity provider: " + displayName + " (" + providerKey + ").",
				providerKey, displayName, loginUrl);
	}

}
