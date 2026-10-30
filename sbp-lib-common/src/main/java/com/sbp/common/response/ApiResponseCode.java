package com.sbp.common.response;

/**
 * Cross-service response codes. Services may also use custom {@code String} codes.
 */
public enum ApiResponseCode {

	SUCCESS("SUCCESS"),
	CREATED("CREATED"),
	VALIDATION_ERROR("VALIDATION_ERROR"),
	NOT_FOUND("NOT_FOUND"),
	UNAUTHORIZED("UNAUTHORIZED"),
	FORBIDDEN("FORBIDDEN"),
	CONFLICT("CONFLICT"),
	INTERNAL_ERROR("INTERNAL_ERROR");

	private final String code;

	ApiResponseCode(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

}
