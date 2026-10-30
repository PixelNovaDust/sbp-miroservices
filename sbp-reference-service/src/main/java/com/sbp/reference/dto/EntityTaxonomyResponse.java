package com.sbp.reference.dto;

import java.time.Instant;
import java.util.UUID;

public record EntityTaxonomyResponse(
		UUID id,
		UUID entityId,
		String entityType,
		UUID taxonomyId,
		Instant createdAt) {
}
