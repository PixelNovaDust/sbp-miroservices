package com.sbp.common.storage;

import java.util.Locale;
import java.util.Map;

public final class StorageMimeTypes {

	private static final Map<String, String> BY_EXT = Map.ofEntries(
			Map.entry("jpg", "image/jpeg"),
			Map.entry("jpeg", "image/jpeg"),
			Map.entry("png", "image/png"),
			Map.entry("gif", "image/gif"),
			Map.entry("webp", "image/webp"),
			Map.entry("mp4", "video/mp4"),
			Map.entry("webm", "video/webm"),
			Map.entry("pdf", "application/pdf"),
			Map.entry("json", "application/json"),
			Map.entry("zip", "application/zip"));

	private StorageMimeTypes() {
	}

	public static String fromFilename(String filename) {
		if (filename == null || filename.isBlank()) {
			return "application/octet-stream";
		}
		int dot = filename.lastIndexOf('.');
		if (dot < 0 || dot == filename.length() - 1) {
			return "application/octet-stream";
		}
		String ext = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
		return BY_EXT.getOrDefault(ext, "application/octet-stream");
	}

}
