package com.sbp.studio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudioPatchRequest(
		@Size(max = 150) String name,
		@Size(max = 150) String slug,
		String website,
		String description,
		Boolean active,
		@NotBlank @Size(max = 100) String actor) {
}
