package com.sbp.common.client;

import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sbp.common.response.ApiResponse;

/**
 * Helpers for typed {@link ApiResponse} bodies from {@link org.springframework.web.client.RestClient}.
 */
public final class ApiClientSupport {

	private ApiClientSupport() {
	}

	public static <T> ParameterizedTypeReference<ApiResponse<T>> apiResponseType(Class<T> dataType) {
		return ParameterizedTypeReference.forType(
				TypeFactory.defaultInstance().constructParametricType(ApiResponse.class, dataType));
	}

	public static <T> T requireData(ApiResponse<T> response) {
		if (response == null || !response.success()) {
			throw new IllegalStateException("Upstream API call failed");
		}
		return response.data();
	}

}
