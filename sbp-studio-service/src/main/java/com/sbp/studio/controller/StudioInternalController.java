package com.sbp.studio.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.studio.dto.StudioResponse;
import com.sbp.studio.service.StudioService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class StudioInternalController {

	private final StudioService studioService;

	public StudioInternalController(StudioService studioService) {
		this.studioService = studioService;
	}

	@GetMapping("/internal/{studioId}")
	public ResponseEntity<ApiResponse<StudioResponse>> getInternal(
			@PathVariable UUID studioId,
			HttpServletRequest request) {
		StudioResponse data = studioService.getById(studioId);
		ApiResponse<StudioResponse> body = ApiResponseFactory.success(
				"STUDIO_FETCHED",
				"Studio retrieved",
				data,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(body);
	}

}
