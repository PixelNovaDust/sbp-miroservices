package com.sbp.reference.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.reference.dto.EntityTaxonomyAssignRequest;
import com.sbp.reference.dto.EntityTaxonomyResponse;
import com.sbp.reference.service.EntityTaxonomyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/entity-taxonomies")
public class EntityTaxonomyInternalController {

	private final EntityTaxonomyService entityTaxonomyService;

	public EntityTaxonomyInternalController(EntityTaxonomyService entityTaxonomyService) {
		this.entityTaxonomyService = entityTaxonomyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<EntityTaxonomyResponse>>> list(
			@RequestParam UUID entityId,
			@RequestParam String entityType,
			HttpServletRequest request) {
		List<EntityTaxonomyResponse> data = entityTaxonomyService.listByEntity(entityId, entityType);
		ApiResponse<List<EntityTaxonomyResponse>> body = ApiResponseFactory.success(
				"ENTITY_TAXONOMIES_LISTED",
				"Entity taxonomies retrieved",
				data,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<EntityTaxonomyResponse>> assign(
			@Valid @RequestBody EntityTaxonomyAssignRequest body,
			HttpServletRequest request) {
		EntityTaxonomyResponse data = entityTaxonomyService.assign(body);
		ApiResponse<EntityTaxonomyResponse> response = ApiResponseFactory.success(
				"ENTITY_TAXONOMY_ASSIGNED",
				"Taxonomy assigned to entity",
				data,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> remove(
			@RequestParam UUID entityId,
			@RequestParam String entityType,
			@RequestParam UUID taxonomyId,
			HttpServletRequest request) {
		entityTaxonomyService.remove(entityId, entityType, taxonomyId);
		ApiResponse<Void> body = ApiResponseFactory.success(
				"ENTITY_TAXONOMY_REMOVED",
				"Taxonomy removed from entity",
				null,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(body);
	}

}
