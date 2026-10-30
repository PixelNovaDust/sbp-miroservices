package com.sbp.person.client.dto;

import java.time.Instant;
import java.util.UUID;

public record SocialPlatformClientResponse(
		UUID id,
		String name,
		String slug,
		String baseUrl,
		String svgIcon,
		String brandColor,
		boolean active,
		Instant createdAt,
		Instant updatedAt,
		String createdBy,
		String updatedBy,
		int version) {
}
