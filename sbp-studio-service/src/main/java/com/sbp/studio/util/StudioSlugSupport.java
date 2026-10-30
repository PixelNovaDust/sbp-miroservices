package com.sbp.studio.util;

import java.util.Locale;
import java.util.regex.Pattern;

import com.sbp.studio.exception.StudioValidationException;

public final class StudioSlugSupport {

	private static final Pattern ALLOWED = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
	/** PowerShell Get-Random suffix accidentally appended to slug in seed scripts */
	private static final Pattern ACCIDENTAL_RANDOM_SUFFIX = Pattern.compile("-\\d{6,}$");

	private StudioSlugSupport() {
	}

	public static String resolveForCreate(String slug, String name) {
		if (slug != null && !slug.isBlank()) {
			String trimmed = slug.trim();
			if (ACCIDENTAL_RANDOM_SUFFIX.matcher(trimmed.toLowerCase(Locale.ROOT)).find()) {
				return normalize(name);
			}
			return normalize(trimmed);
		}
		if (name == null || name.isBlank()) {
			throw new StudioValidationException("Studio name is required to derive slug.");
		}
		return normalize(name);
	}

	public static String normalize(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new StudioValidationException("Slug cannot be empty.");
		}
		String normalized = raw.trim().toLowerCase(Locale.ROOT)
				.replaceAll("[^a-zA-Z0-9]+", "-")
				.replaceAll("-{2,}", "-")
				.replaceAll("^-|-$", "");
		if (normalized.isEmpty()) {
			throw new StudioValidationException("Slug is invalid after normalization.");
		}
		if (!ALLOWED.matcher(normalized).matches()) {
			throw new StudioValidationException("Slug must be lowercase letters, numbers, and hyphens only.");
		}
		return normalized;
	}

}
