package com.sbp.person.exception;

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

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.VALIDATION_ERROR,
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.NOT_FOUND,
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

}
