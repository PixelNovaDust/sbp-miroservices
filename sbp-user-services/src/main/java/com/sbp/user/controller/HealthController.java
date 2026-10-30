package com.sbp.user.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HealthController {

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

}
