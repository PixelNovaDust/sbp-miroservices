package com.sbp.common.storage;

public record PresignUploadRequest(
		String filename,
		String bucket,
		String keyPrefix,
		String objectKey,
		String contentType,
		long contentLength) {

	public PresignUploadRequest {
		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("filename is required");
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String filename;
		private String bucket;
		private String keyPrefix;
		private String objectKey;
		private String contentType;
		private long contentLength = -1;

		public Builder filename(String filename) {
			this.filename = filename;
			return this;
		}

		public Builder bucket(String bucket) {
			this.bucket = bucket;
			return this;
		}

		public Builder keyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
			return this;
		}

		public Builder objectKey(String objectKey) {
			this.objectKey = objectKey;
			return this;
		}

		public Builder contentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		public Builder contentLength(long contentLength) {
			this.contentLength = contentLength;
			return this;
		}

		public PresignUploadRequest build() {
			return new PresignUploadRequest(filename, bucket, keyPrefix, objectKey, contentType, contentLength);
		}
	}
}
