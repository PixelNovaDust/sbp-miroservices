package com.sbp.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaxonomyUpdateRequest(
		@Size(max = 50) String type,
		@Size(max = 100) String name,
		@Size(max = 100) String slug,
		String description,
		Boolean active,
		@NotBlank @Size(max = 100) String actor) {
}
