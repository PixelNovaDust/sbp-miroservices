package com.sbp.common.storage;

public record ObjectHead(
		String bucket,
		String objectKey,
		long sizeBytes,
		String etag,
		String mimeType) {
}
