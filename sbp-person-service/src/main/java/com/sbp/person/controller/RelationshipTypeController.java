package com.sbp.person.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.person.dto.LookupTypeCreateRequest;
import com.sbp.person.dto.LookupTypeResponse;
import com.sbp.person.service.RelationshipTypeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/relationship-types")
public class RelationshipTypeController {

	private final RelationshipTypeService relationshipTypeService;

	public RelationshipTypeController(RelationshipTypeService relationshipTypeService) {
		this.relationshipTypeService = relationshipTypeService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<LookupTypeResponse>>> list(
			@RequestParam(required = false) Boolean active,
			HttpServletRequest request) {
		List<LookupTypeResponse> data = relationshipTypeService.list(active);
		return ResponseEntity.ok(success("RELATIONSHIP_TYPES_LISTED", "Relationship types retrieved", data, request));
	}

	@GetMapping("/by-slug/{slug}")
	public ResponseEntity<ApiResponse<LookupTypeResponse>> getBySlug(
			@PathVariable String slug,
			HttpServletRequest request) {
		LookupTypeResponse data = relationshipTypeService.getBySlug(slug);
		return ResponseEntity.ok(success("RELATIONSHIP_TYPE_FETCHED", "Relationship type retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<LookupTypeResponse>> create(
			@Valid @RequestBody LookupTypeCreateRequest body,
			HttpServletRequest request) {
		LookupTypeResponse data = relationshipTypeService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("RELATIONSHIP_TYPE_CREATED", "Relationship type created", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
