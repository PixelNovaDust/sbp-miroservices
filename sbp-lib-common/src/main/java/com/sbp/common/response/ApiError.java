package com.sbp.common.response;

/**
 * Field-level or application error detail.
 */
public record ApiError(String field, String code, String message) {

	public static ApiError of(String field, String code, String message) {
		return new ApiError(field, code, message);
	}

	public static ApiError global(String code, String message) {
		return new ApiError(null, code, message);
	}

}
