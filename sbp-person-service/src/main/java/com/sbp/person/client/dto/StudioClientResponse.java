package com.sbp.person.client.dto;

import java.time.Instant;
import java.util.UUID;

public record StudioClientResponse(
		UUID id,
		String name,
		String slug,
		String website,
		String description,
		boolean active,
		StudioAuditClientResponse audit) {

	public record StudioAuditClientResponse(Instant createdAt, Instant updatedAt, int version) {
	}

}
