package com.sbp.person.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;

public record PersonPhysicalAttributesRequest(
		Short bust,
		Short waist,
		Short hips,
		UUID bodyTypeId,
		@Size(max = 50) String eyeColor,
		@Size(max = 50) String hairColor,
		@Size(max = 20) String unit) {
}
