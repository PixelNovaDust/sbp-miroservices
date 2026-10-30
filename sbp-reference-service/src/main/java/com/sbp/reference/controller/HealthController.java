package com.sbp.reference.controller;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HealthController {

	private final JdbcTemplate jdbcTemplate;

	public HealthController(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@GetMapping("/health")
	public ResponseEntity<ApiResponse<Map<String, String>>> health(HttpServletRequest request) {
		ApiResponse<Map<String, String>> body = ApiResponseFactory.success(
				"HEALTH_OK",
				"Service is healthy",
				Map.of("status", "UP"),
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(body);
	}

	@GetMapping("/ready")
	public ResponseEntity<ApiResponse<Map<String, String>>> ready(HttpServletRequest request) {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			ApiResponse<Map<String, String>> body = ApiResponseFactory.success(
					"READY_OK",
					"Service is ready",
					Map.of("status", "UP", "database", "UP"),
					request.getRequestURI(),
					request.getHeader("X-Trace-Id"));
			return ResponseEntity.ok(body);
		}
		catch (Exception ex) {
			ApiResponse<Map<String, String>> body = ApiResponseFactory.error(
					"NOT_READY",
					"Database check failed",
					request.getRequestURI(),
					request.getHeader("X-Trace-Id"));
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
		}
	}

}
