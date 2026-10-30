package com.sbp.person.client.dto;

import java.time.Instant;
import java.util.UUID;

public record TaxonomyClientResponse(
		UUID id,
		String type,
		String name,
		String slug,
		String description,
		boolean active,
		Instant createdAt,
		Instant updatedAt,
		String createdBy,
		String updatedBy,
		int version) {
}
