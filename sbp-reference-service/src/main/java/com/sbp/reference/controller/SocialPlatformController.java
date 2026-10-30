package com.sbp.reference.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.reference.dto.SocialPlatformCreateRequest;
import com.sbp.reference.dto.SocialPlatformResponse;
import com.sbp.reference.dto.SocialPlatformUpdateRequest;
import com.sbp.reference.service.SocialPlatformService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/social-platforms")
public class SocialPlatformController {

	private final SocialPlatformService socialPlatformService;

	public SocialPlatformController(SocialPlatformService socialPlatformService) {
		this.socialPlatformService = socialPlatformService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<SocialPlatformResponse>>> list(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Boolean active,
			HttpServletRequest request) {
		List<SocialPlatformResponse> data = socialPlatformService.list(name, active);
		return ResponseEntity.ok(success("SOCIAL_PLATFORMS_LISTED", "Social platforms retrieved", data, request));
	}

	@GetMapping("/by-slug/{slug}")
	public ResponseEntity<ApiResponse<SocialPlatformResponse>> getBySlug(
			@PathVariable String slug,
			HttpServletRequest request) {
		SocialPlatformResponse data = socialPlatformService.getBySlug(slug);
		return ResponseEntity.ok(success("SOCIAL_PLATFORM_FETCHED", "Social platform retrieved", data, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<SocialPlatformResponse>> getById(
			@PathVariable UUID id,
			HttpServletRequest request) {
		SocialPlatformResponse data = socialPlatformService.getById(id);
		return ResponseEntity.ok(success("SOCIAL_PLATFORM_FETCHED", "Social platform retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<SocialPlatformResponse>> create(
			@Valid @RequestBody SocialPlatformCreateRequest body,
			HttpServletRequest request) {
		SocialPlatformResponse data = socialPlatformService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("SOCIAL_PLATFORM_CREATED", "Social platform created", data, request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<SocialPlatformResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody SocialPlatformUpdateRequest body,
			HttpServletRequest request) {
		SocialPlatformResponse data = socialPlatformService.update(id, body);
		return ResponseEntity.ok(success("SOCIAL_PLATFORM_UPDATED", "Social platform updated", data, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<SocialPlatformResponse>> deactivate(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "system") String actor,
			HttpServletRequest request) {
		SocialPlatformResponse data = socialPlatformService.deactivate(id, actor);
		return ResponseEntity.ok(success("SOCIAL_PLATFORM_DEACTIVATED", "Social platform deactivated", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
