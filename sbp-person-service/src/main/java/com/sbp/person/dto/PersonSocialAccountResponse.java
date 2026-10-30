package com.sbp.person.dto;

import java.time.Instant;
import java.util.UUID;

public record PersonSocialAccountResponse(
		UUID id,
		UUID socialPlatformId,
		String username,
		boolean verified,
		boolean available,
		Long followersCount,
		Instant lastSyncedAt,
		Instant createdAt,
		Instant updatedAt,
		int version) {
}
