package com.sbp.studio.exception;

import org.springframework.dao.DataIntegrityViolationException;
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

	@ExceptionHandler(StudioValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(StudioValidationException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.VALIDATION_ERROR,
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(StudioConflictException.class)
	public ResponseEntity<ApiResponse<Void>> handleConflict(StudioConflictException ex, HttpServletRequest request) {
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.CONFLICT,
				ex.getMessage(),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
			DataIntegrityViolationException ex,
			HttpServletRequest request) {
		String message = "Studio name or slug already exists.";
		if (ex.getMessage() != null && ex.getMessage().contains("uq_studios_slug")) {
			message = "Studio slug already exists. Use a different slug or update the existing studio.";
		}
		else if (ex.getMessage() != null && ex.getMessage().contains("uq_studios_name")) {
			message = "Studio name already exists.";
		}
		ApiResponse<Void> body = ApiResponseFactory.error(
				ApiResponseCode.CONFLICT,
				message,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
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
