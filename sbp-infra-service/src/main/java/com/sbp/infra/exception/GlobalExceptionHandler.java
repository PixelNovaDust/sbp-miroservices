package com.sbp.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseCode;
import com.sbp.common.response.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.NOT_FOUND,
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> badState(IllegalStateException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				"FILE_INVALID_STATE",
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

}
