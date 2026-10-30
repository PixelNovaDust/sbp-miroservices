package com.sbp.reference.dto;

import java.time.Instant;
import java.util.UUID;

public record SocialPlatformResponse(
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
