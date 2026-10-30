package com.sbp.studio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudioUpdateRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 150) String slug,
		String website,
		String description,
		@NotBlank @Size(max = 100) String actor) {
}
