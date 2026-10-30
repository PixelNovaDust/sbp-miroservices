package com.sbp.studio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudioCreateRequest(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 150) String slug,
		String website,
		String description,
		@NotBlank @Size(max = 100) String actor) {
}
