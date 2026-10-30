package com.sbp.admin.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.sbp.admin.dto.MonitoringDtos.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class MonitoringApiExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
				ex.getReason(), request.getRequestURI()));
	}

}
