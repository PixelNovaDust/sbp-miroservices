package com.sbp.person.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PersonProfileResponse(
		UUID id,
		boolean active,
		String name,
		String stageName,
		String legalName,
		String bio,
		LocalDate birthDate,
		String birthPlace,
		String nationality,
		String gender,
		String genderDisplay,
		Integer height,
		Integer weight,
		boolean verified,
		Integer debutYear,
		String careerStatus,
		Integer retiredYear,
		Integer lastActiveYear,
		String profilePath,
		String backdropPath,
		String contentType,
		List<String> aliases,
		Measurements measurements,
		List<TaxonomyItem> categories,
		List<TaxonomyItem> tags,
		List<TaxonomyItem> specialties,
		List<TaxonomyItem> roles,
		Career career,
		List<SocialPresence> socialPresence,
		Map<String, Object> sourceMetadata,
		Map<String, Object> stats,
		Audit audit) {

	public record Audit(Instant createdAt, Instant updatedAt, int version) {
	}

	public record Measurements(
			Integer bust,
			Integer waist,
			Integer hips,
			String unit,
			String eyeColor,
			String hairColor,
			UUID bodyTypeId,
			String bodyType) {
	}

	public record TaxonomyItem(UUID id, String name, String slug, String type) {
	}

	public record Career(
			String details,
			List<String> highlights,
			List<String> knownFor,
			List<FeaturedContentItem> featuredContent) {
	}

	public record FeaturedContentItem(UUID contentId, String title, String slug) {
	}

	public record SocialPresence(
			UUID platformId,
			String platformSlug,
			String username,
			boolean verified,
			boolean available) {
	}

}
