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
import com.sbp.reference.dto.TaxonomyCreateRequest;
import com.sbp.reference.dto.TaxonomyResponse;
import com.sbp.reference.dto.TaxonomyUpdateRequest;
import com.sbp.reference.service.TaxonomyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/taxonomies")
public class TaxonomyController {

	private final TaxonomyService taxonomyService;

	public TaxonomyController(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<TaxonomyResponse>>> list(
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Boolean active,
			HttpServletRequest request) {
		List<TaxonomyResponse> data = taxonomyService.list(type, name, active);
		return ResponseEntity.ok(success("TAXONOMIES_LISTED", "Taxonomies retrieved", data, request));
	}

	@GetMapping("/types")
	public ResponseEntity<ApiResponse<List<String>>> listTypes(HttpServletRequest request) {
		List<String> data = taxonomyService.listTypes();
		return ResponseEntity.ok(success("TAXONOMY_TYPES_LISTED", "Taxonomy types retrieved", data, request));
	}

	@GetMapping("/by-slug/{slug}")
	public ResponseEntity<ApiResponse<TaxonomyResponse>> getBySlug(
			@PathVariable String slug,
			HttpServletRequest request) {
		TaxonomyResponse data = taxonomyService.getBySlug(slug);
		return ResponseEntity.ok(success("TAXONOMY_FETCHED", "Taxonomy retrieved", data, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TaxonomyResponse>> getById(
			@PathVariable UUID id,
			HttpServletRequest request) {
		TaxonomyResponse data = taxonomyService.getById(id);
		return ResponseEntity.ok(success("TAXONOMY_FETCHED", "Taxonomy retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TaxonomyResponse>> create(
			@Valid @RequestBody TaxonomyCreateRequest body,
			HttpServletRequest request) {
		TaxonomyResponse data = taxonomyService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("TAXONOMY_CREATED", "Taxonomy created", data, request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<TaxonomyResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody TaxonomyUpdateRequest body,
			HttpServletRequest request) {
		TaxonomyResponse data = taxonomyService.update(id, body);
		return ResponseEntity.ok(success("TAXONOMY_UPDATED", "Taxonomy updated", data, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<TaxonomyResponse>> deactivate(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "system") String actor,
			HttpServletRequest request) {
		TaxonomyResponse data = taxonomyService.deactivate(id, actor);
		return ResponseEntity.ok(success("TAXONOMY_DEACTIVATED", "Taxonomy deactivated", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
