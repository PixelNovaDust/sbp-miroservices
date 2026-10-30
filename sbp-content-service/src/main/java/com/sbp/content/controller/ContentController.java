package com.sbp.content.controller;

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
import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;
import com.sbp.content.dto.ContentDetailResponse;
import com.sbp.content.dto.ContentSummaryResponse;
import com.sbp.content.dto.ContentWriteRequest;
import com.sbp.content.entity.Content;
import com.sbp.content.service.ContentAggregateService;
import com.sbp.content.service.ContentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping
public class ContentController {

	private final ContentService contentService;

	private final ContentAggregateService contentAggregateService;

	public ContentController(ContentService contentService, ContentAggregateService contentAggregateService) {
		this.contentService = contentService;
		this.contentAggregateService = contentAggregateService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ContentSummaryResponse>>> list(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) ContentType contentType,
			@RequestParam(required = false) ContentStatus status,
			@RequestParam(required = false) ContentVisibility visibility,
			HttpServletRequest request) {
		List<ContentSummaryResponse> data = contentService.list(title, contentType, status, visibility);
		return ResponseEntity.ok(success("CONTENT_LISTED", "Content items retrieved", data, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ContentDetailResponse>> getDetail(
			@PathVariable UUID id,
			HttpServletRequest request) {
		ContentDetailResponse data = contentAggregateService.getDetail(id);
		return ResponseEntity.ok(success("CONTENT_FETCHED", "Content retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ContentDetailResponse>> create(
			@Valid @RequestBody ContentWriteRequest body,
			HttpServletRequest request) {
		Content created = contentService.create(body);
		ContentDetailResponse data = contentAggregateService.getDetail(created.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("CONTENT_CREATED", "Content created", data, request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ContentDetailResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody ContentWriteRequest body,
			HttpServletRequest request) {
		contentService.update(id, body);
		ContentDetailResponse data = contentAggregateService.getDetail(id);
		return ResponseEntity.ok(success("CONTENT_UPDATED", "Content updated", data, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id, HttpServletRequest request) {
		contentService.delete(id);
		return ResponseEntity.ok(success("CONTENT_DELETED", "Content deleted", null, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
