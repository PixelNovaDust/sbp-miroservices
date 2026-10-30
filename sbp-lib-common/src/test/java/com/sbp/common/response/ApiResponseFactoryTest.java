package com.sbp.common.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ApiResponseFactoryTest {

	@Test
	void successResponse() {
		ApiResponse<Map<String, String>> response = ApiResponseFactory.success("USER_FETCHED", "User retrieved",
				Map.of("id", "usr_123"), "/api/v1/users/usr_123", "trace-1");

		assertTrue(response.success());
		assertEquals("USER_FETCHED", response.code());
		assertEquals("usr_123", response.data().get("id"));
		assertNull(response.errors());
		assertNull(response.meta());
	}

	@Test
	void validationErrorResponse() {
		List<ApiError> errors = List.of(ApiError.of("email", "INVALID_FORMAT", "Email address is invalid"));
		ApiResponse<Void> response = ApiResponseFactory.validationError(errors, "/api/v1/auth/register", "trace-2");

		assertFalse(response.success());
		assertEquals("VALIDATION_ERROR", response.code());
		assertEquals(1, response.errors().size());
	}

	@Test
	void paginatedResponse() {
		PageMetadata page = PageMetadata.of(0, 10, 25);
		ApiResponse<List<String>> response = ApiResponseFactory.paginated("USERS_FETCHED", "Users retrieved",
				List.of("usr_1", "usr_2"), page, "/api/v1/users", "trace-3");

		assertTrue(response.success());
		assertNotNull(response.meta());
		assertTrue(response.meta().containsKey("pagination"));
		PageMetadata fromMeta = (PageMetadata) response.meta().get("pagination");
		assertEquals(3, fromMeta.totalPages());
		assertTrue(fromMeta.hasNext());
	}

}
