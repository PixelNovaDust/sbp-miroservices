package com.sbp.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic API response envelope. Optional fields ({@code errors}, {@code meta}) may be {@code null}
 * when not applicable; they are omitted from JSON via {@link JsonInclude}.
 *
 * @param <T> payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data,
		Instant timestamp,
		String path,
		List<ApiError> errors,
		Map<String, Object> meta,
		String traceId) {

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static final class Builder<T> {

		private boolean success;

		private String code;

		private String message;

		private T data;

		private Instant timestamp = Instant.now();

		private String path;

		private List<ApiError> errors;

		private Map<String, Object> meta;

		private String traceId;

		public Builder<T> success(boolean success) {
			this.success = success;
			return this;
		}

		public Builder<T> code(String code) {
			this.code = code;
			return this;
		}

		public Builder<T> code(ApiResponseCode code) {
			this.code = code.code();
			return this;
		}

		public Builder<T> message(String message) {
			this.message = message;
			return this;
		}

		public Builder<T> data(T data) {
			this.data = data;
			return this;
		}

		public Builder<T> timestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder<T> path(String path) {
			this.path = path;
			return this;
		}

		public Builder<T> errors(List<ApiError> errors) {
			this.errors = errors;
			return this;
		}

		public Builder<T> meta(Map<String, Object> meta) {
			this.meta = meta;
			return this;
		}

		public Builder<T> traceId(String traceId) {
			this.traceId = traceId;
			return this;
		}

		public ApiResponse<T> build() {
			return new ApiResponse<>(success, code, message, data, timestamp, path, errors, meta, traceId);
		}

	}

}
