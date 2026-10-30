package com.sbp.person.client.dto;

import java.time.Instant;
import java.util.UUID;

public record EntityTaxonomyClientResponse(
		UUID id,
		UUID entityId,
		String entityType,
		UUID taxonomyId,
		Instant createdAt) {
}
