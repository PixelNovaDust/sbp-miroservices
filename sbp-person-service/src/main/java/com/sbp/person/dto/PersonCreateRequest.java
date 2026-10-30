package com.sbp.person.dto;

import java.time.LocalDate;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonCreateRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 150) String slug,
		@Size(max = 150) String stageName,
		@NotBlank @Size(max = 150) String legalName,
		String bio,
		LocalDate birthDate,
		@Size(max = 255) String birthPlace,
		@Size(max = 100) String nationality,
		@Size(max = 30) String gender,
		Short heightCm,
		Short weightKg,
		Boolean verified,
		Short debutYear,
		@Size(max = 50) String careerStatus,
		Short retiredYear,
		Short lastActiveYear,
		String profilePath,
		String backdropPath,
		PersonCareerInput career,
		Map<String, Object> sourceMetadata,
		@NotBlank @Size(max = 100) String actor) {
}
