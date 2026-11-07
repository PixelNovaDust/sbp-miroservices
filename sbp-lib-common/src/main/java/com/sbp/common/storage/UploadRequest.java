package com.sbp.common.storage;

import java.io.InputStream;

public record UploadRequest(
		InputStream content,
		long contentLength,
		String filename,
		String bucket,
		String keyPrefix,
		String objectKey,
		String contentType) {

	public UploadRequest {
		if (content == null) {
			throw new IllegalArgumentException("content is required");
		}
		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("filename is required");
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private InputStream content;
		private long contentLength = -1;
		private String filename;
		private String bucket;
		private String keyPrefix;
		private String objectKey;
		private String contentType;

		public Builder content(InputStream content) {
			this.content = content;
			return this;
		}

		public Builder contentLength(long contentLength) {
			this.contentLength = contentLength;
			return this;
		}

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

		public UploadRequest build() {
			return new UploadRequest(content, contentLength, filename, bucket, keyPrefix, objectKey, contentType);
		}
	}
}
