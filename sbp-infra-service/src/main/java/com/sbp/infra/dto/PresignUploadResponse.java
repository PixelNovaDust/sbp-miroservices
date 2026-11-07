package com.sbp.infra.dto;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PresignUploadResponse(
		UUID fileId,
		URL uploadUrl,
		String bucketName,
		String objectKey,
		String mimeType,
		Instant expiresAt,
		Map<String, List<String>> signedHeaders) {
}
