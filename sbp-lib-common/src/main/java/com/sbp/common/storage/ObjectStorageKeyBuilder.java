package com.sbp.common.storage;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Builds safe, unique object keys under an optional global and logical prefix.
 */
public final class ObjectStorageKeyBuilder {

	private static final Pattern UNSAFE = Pattern.compile("[^a-zA-Z0-9._-]+");

	private ObjectStorageKeyBuilder() {
	}

	public static String build(String globalPrefix, String logicalPrefix, String originalFilename) {
		String safeName = sanitizeFilename(originalFilename);
		String unique = UUID.randomUUID().toString();
		String segment = logicalPrefix == null || logicalPrefix.isBlank()
				? unique + "-" + safeName
				: trimSlashes(logicalPrefix) + "/" + unique + "-" + safeName;
		if (globalPrefix == null || globalPrefix.isBlank()) {
			return segment;
		}
		return trimSlashes(globalPrefix) + "/" + segment;
	}

	public static String objectReference(String provider, String bucket, String objectKey) {
		return provider.toLowerCase(Locale.ROOT) + "://" + bucket + "/" + objectKey;
	}

	public static String sanitizeFilename(String filename) {
		if (filename == null || filename.isBlank()) {
			return "file";
		}
		String base = filename.replace('\\', '/');
		int slash = base.lastIndexOf('/');
		if (slash >= 0 && slash < base.length() - 1) {
			base = base.substring(slash + 1);
		}
		base = Normalizer.normalize(base, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
		base = UNSAFE.matcher(base).replaceAll("-");
		base = base.replaceAll("-+", "-").strip();
		if (base.isBlank()) {
			return "file";
		}
		return base.length() > 200 ? base.substring(0, 200) : base;
	}

	private static String trimSlashes(String value) {
		String trimmed = value.strip();
		while (trimmed.startsWith("/")) {
			trimmed = trimmed.substring(1);
		}
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

}
