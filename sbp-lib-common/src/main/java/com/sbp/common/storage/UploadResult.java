package com.sbp.common.storage;

public record UploadResult(
		String bucket,
		String objectKey,
		String objectReference,
		String etag,
		long sizeBytes,
		String mimeType,
		StorageProvider storageProvider,
		String region) {
}
