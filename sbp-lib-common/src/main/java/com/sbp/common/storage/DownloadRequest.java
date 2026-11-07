package com.sbp.common.storage;

public record DownloadRequest(String bucket, String objectKey, String contentType) {

	public DownloadRequest {
		if (bucket == null || bucket.isBlank()) {
			throw new IllegalArgumentException("bucket is required");
		}
		if (objectKey == null || objectKey.isBlank()) {
			throw new IllegalArgumentException("objectKey is required");
		}
	}

	public static DownloadRequest of(String bucket, String objectKey) {
		return new DownloadRequest(bucket, objectKey, null);
	}
}
