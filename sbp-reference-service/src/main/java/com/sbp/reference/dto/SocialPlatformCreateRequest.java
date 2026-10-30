package com.sbp.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialPlatformCreateRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 100) String slug,
		@NotBlank @Size(max = 255) String baseUrl,
		String svgIcon,
		@Size(max = 20) String brandColor,
		Boolean active,
		@NotBlank @Size(max = 100) String actor) {
}
