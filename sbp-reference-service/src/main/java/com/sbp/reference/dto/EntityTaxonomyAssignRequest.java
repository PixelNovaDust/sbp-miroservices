package com.sbp.reference.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntityTaxonomyAssignRequest(
		@NotNull UUID entityId,
		@NotBlank @Size(max = 50) String entityType,
		@NotNull UUID taxonomyId) {
}
