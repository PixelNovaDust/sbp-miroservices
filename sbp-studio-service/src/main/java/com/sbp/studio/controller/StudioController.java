package com.sbp.studio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.studio.dto.StudioCreateRequest;
import com.sbp.studio.dto.StudioPatchRequest;
import com.sbp.studio.dto.StudioResponse;
import com.sbp.studio.dto.StudioUpdateRequest;
import com.sbp.studio.service.StudioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "", "/" })
public class StudioController {

	private final StudioService studioService;

	public StudioController(StudioService studioService) {
		this.studioService = studioService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<StudioResponse>>> list(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Boolean active,
			HttpServletRequest request) {
		List<StudioResponse> data = studioService.list(name, active);
		return ResponseEntity.ok(success("STUDIOS_LISTED", "Studios retrieved", data, request));
	}

	@GetMapping("/by-name/{name}")
	public ResponseEntity<ApiResponse<StudioResponse>> getByName(
			@PathVariable String name,
			HttpServletRequest request) {
		StudioResponse data = studioService.getByName(name);
		return ResponseEntity.ok(success("STUDIO_FETCHED", "Studio retrieved", data, request));
	}

	@GetMapping("/{studioId}")
	public ResponseEntity<ApiResponse<StudioResponse>> getById(
			@PathVariable UUID studioId,
			HttpServletRequest request) {
		StudioResponse data = studioService.getById(studioId);
		return ResponseEntity.ok(success("STUDIO_FETCHED", "Studio retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<StudioResponse>> create(
			@Valid @RequestBody StudioCreateRequest body,
			HttpServletRequest request) {
		StudioResponse data = studioService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("STUDIO_CREATED", "Studio created", data, request));
	}

	@PutMapping("/{studioId}")
	public ResponseEntity<ApiResponse<StudioResponse>> update(
			@PathVariable UUID studioId,
			@Valid @RequestBody StudioUpdateRequest body,
			HttpServletRequest request) {
		StudioResponse data = studioService.update(studioId, body);
		return ResponseEntity.ok(success("STUDIO_UPDATED", "Studio updated", data, request));
	}

	@PatchMapping("/{studioId}")
	public ResponseEntity<ApiResponse<StudioResponse>> patch(
			@PathVariable UUID studioId,
			@Valid @RequestBody StudioPatchRequest body,
			HttpServletRequest request) {
		StudioResponse data = studioService.patch(studioId, body);
		return ResponseEntity.ok(success("STUDIO_PATCHED", "Studio updated", data, request));
	}

	@DeleteMapping("/{studioId}")
	public ResponseEntity<ApiResponse<StudioResponse>> softDelete(
			@PathVariable UUID studioId,
			@RequestParam(defaultValue = "system") String actor,
			HttpServletRequest request) {
		StudioResponse data = studioService.softDelete(studioId, actor);
		return ResponseEntity.ok(success("STUDIO_DEACTIVATED", "Studio deactivated", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
