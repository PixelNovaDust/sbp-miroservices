package com.sbp.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LookupTypeCreateRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 100) String slug,
		String description,
		@NotBlank @Size(max = 100) String actor) {
}
