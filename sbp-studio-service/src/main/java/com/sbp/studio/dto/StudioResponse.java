package com.sbp.studio.dto;

import java.util.UUID;

public record StudioResponse(
		UUID id,
		String name,
		String slug,
		String website,
		String description,
		boolean active,
		StudioAuditResponse audit) {
}
