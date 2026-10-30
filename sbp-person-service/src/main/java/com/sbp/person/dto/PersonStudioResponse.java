package com.sbp.person.dto;

import java.time.Instant;
import java.util.UUID;

public record PersonStudioResponse(
		UUID id,
		UUID studioId,
		UUID relationshipTypeId,
		String relationshipTypeSlug,
		Instant createdAt) {
}
