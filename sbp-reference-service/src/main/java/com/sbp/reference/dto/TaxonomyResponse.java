package com.sbp.reference.dto;

import java.time.Instant;
import java.util.UUID;

public record TaxonomyResponse(
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
