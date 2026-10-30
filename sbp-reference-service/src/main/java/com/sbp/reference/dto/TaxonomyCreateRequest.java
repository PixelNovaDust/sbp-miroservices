package com.sbp.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaxonomyCreateRequest(
		@NotBlank @Size(max = 50) String type,
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 100) String slug,
		String description,
		Boolean active,
		@NotBlank @Size(max = 100) String actor) {
}
