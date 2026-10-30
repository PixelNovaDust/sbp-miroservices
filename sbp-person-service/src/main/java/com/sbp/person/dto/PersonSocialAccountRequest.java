package com.sbp.person.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PersonSocialAccountRequest(
		@NotNull UUID socialPlatformId,
		@NotBlank @Size(max = 150) String username,
		Boolean verified,
		Boolean available,
		Long followersCount,
		@NotBlank @Size(max = 100) String actor) {
}
