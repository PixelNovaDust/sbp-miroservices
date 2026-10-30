package com.sbp.reference.service;

import java.util.Locale;
import java.util.Set;

import com.sbp.reference.exception.InvalidTaxonomyTypeException;
import com.sbp.reference.repository.TaxonomyTypeRepository;

final class TaxonomyTypeSupport {

	static final Set<String> KNOWN_CODES = Set.of(
			"CATEGORY",
			"TAG",
			"SPECIALTIES",
			"MOOD_TAG",
			"GENRE",
			"ROLE");

	private TaxonomyTypeSupport() {
	}

	static String normalizeType(String rawType) {
		if (rawType == null || rawType.isBlank()) {
			throw new InvalidTaxonomyTypeException("Taxonomy type is required.");
		}
		String normalized = rawType.trim().toUpperCase(Locale.ROOT);
		if ("SPECIALTY".equals(normalized) || "SPECIALITIES".equals(normalized)) {
			return "SPECIALTIES";
		}
		return normalized;
	}

	static void assertAllowedType(TaxonomyTypeRepository taxonomyTypeRepository, String rawType) {
		String normalized = normalizeType(rawType);
		if (!KNOWN_CODES.contains(normalized)) {
			throw new InvalidTaxonomyTypeException(
					"Invalid taxonomy type '" + rawType + "'. Allowed: " + String.join(", ", KNOWN_CODES));
		}
		if (!taxonomyTypeRepository.existsById(normalized)) {
			throw new InvalidTaxonomyTypeException(
					"Taxonomy type '" + normalized + "' is not registered in taxonomy_types.");
		}
	}

}
