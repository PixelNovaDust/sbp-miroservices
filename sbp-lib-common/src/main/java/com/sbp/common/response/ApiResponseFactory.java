package com.sbp.common.response;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Factory helpers for common {@link ApiResponse} shapes.
 */
public final class ApiResponseFactory {

	private ApiResponseFactory() {
	}

	public static <T> ApiResponse<T> success(String code, String message, T data, String path, String traceId) {
		return ApiResponse.<T>builder()
				.success(true)
				.code(code)
				.message(message)
				.data(data)
				.path(path)
				.traceId(traceId)
				.timestamp(Instant.now())
				.build();
	}

	public static <T> ApiResponse<T> success(ApiResponseCode code, String message, T data, String path,
			String traceId) {
		return success(code.code(), message, data, path, traceId);
	}

	public static <T> ApiResponse<List<T>> paginated(String code, String message, List<T> data, PageMetadata pagination,
			String path, String traceId) {
		Map<String, Object> meta = Map.of("pagination", pagination);
		return ApiResponse.<List<T>>builder()
				.success(true)
				.code(code)
				.message(message)
				.data(data)
				.meta(meta)
				.path(path)
				.traceId(traceId)
				.timestamp(Instant.now())
				.build();
	}

	public static <T> ApiResponse<T> error(String code, String message, String path, String traceId) {
		return error(code, message, path, traceId, null);
	}

	public static <T> ApiResponse<T> error(String code, String message, String path, String traceId,
			List<ApiError> errors) {
		return ApiResponse.<T>builder()
				.success(false)
				.code(code)
				.message(message)
				.data(null)
				.path(path)
				.traceId(traceId)
				.errors(errors == null || errors.isEmpty() ? null : List.copyOf(errors))
				.timestamp(Instant.now())
				.build();
	}

	public static <T> ApiResponse<T> error(ApiResponseCode code, String message, String path, String traceId) {
		return error(code.code(), message, path, traceId);
	}

	public static <T> ApiResponse<T> validationError(String message, List<ApiError> errors, String path,
			String traceId) {
		return ApiResponse.<T>builder()
				.success(false)
				.code(ApiResponseCode.VALIDATION_ERROR.code())
				.message(message)
				.data(null)
				.path(path)
				.traceId(traceId)
				.errors(errors == null ? List.of() : List.copyOf(errors))
				.timestamp(Instant.now())
				.build();
	}

	public static <T> ApiResponse<T> validationError(List<ApiError> errors, String path, String traceId) {
		return validationError("Request validation failed", errors, path, traceId);
	}

	public static Map<String, Object> metaOf(String key, Object value) {
		return Collections.singletonMap(key, value);
	}

}
