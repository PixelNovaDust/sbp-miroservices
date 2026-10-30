package com.sbp.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialPlatformUpdateRequest(
		@Size(max = 100) String name,
		@Size(max = 100) String slug,
		@Size(max = 255) String baseUrl,
		String svgIcon,
		@Size(max = 20) String brandColor,
		Boolean active,
		@NotBlank @Size(max = 100) String actor) {
}
