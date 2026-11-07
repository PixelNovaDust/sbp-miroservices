package com.sbp.common.storage;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PresignedUpload(
		URL uploadUrl,
		String bucket,
		String objectKey,
		String objectReference,
		String mimeType,
		Instant expiresAt,
		Map<String, List<String>> signedHeaders) {
}
