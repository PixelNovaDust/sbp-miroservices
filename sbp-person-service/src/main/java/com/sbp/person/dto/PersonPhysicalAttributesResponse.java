package com.sbp.person.dto;

import java.time.Instant;
import java.util.UUID;

public record PersonPhysicalAttributesResponse(
		UUID personId,
		Short bust,
		Short waist,
		Short hips,
		UUID bodyTypeId,
		String bodyTypeName,
		String eyeColor,
		String hairColor,
		String unit,
		Instant updatedAt) {
}
