package com.sbp.person.util;

import java.util.Locale;
import java.util.Set;

import com.sbp.person.exception.BadRequestException;

public final class PersonCareerStatusSupport {

	private static final Set<String> ALLOWED = Set.of("ACTIVE", "RETIRED", "HIATUS", "DEBUT");

	private PersonCareerStatusSupport() {
	}

	public static String normalizeOrNull(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String normalized = raw.trim().toUpperCase(Locale.ROOT);
		if ("Active".equalsIgnoreCase(raw.trim())) {
			return "ACTIVE";
		}
		if (!ALLOWED.contains(normalized)) {
			throw new BadRequestException(
					"Invalid careerStatus '" + raw + "'. Allowed: " + String.join(", ", ALLOWED));
		}
		return normalized;
	}

}
