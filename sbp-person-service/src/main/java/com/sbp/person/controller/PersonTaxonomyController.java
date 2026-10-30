package com.sbp.person.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.person.client.dto.EntityTaxonomyClientResponse;
import com.sbp.person.dto.PersonTaxonomyAssignRequest;
import com.sbp.person.service.PersonTaxonomyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{personId}/taxonomies")
public class PersonTaxonomyController {

	private final PersonTaxonomyService personTaxonomyService;

	public PersonTaxonomyController(PersonTaxonomyService personTaxonomyService) {
		this.personTaxonomyService = personTaxonomyService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<EntityTaxonomyClientResponse>>> list(
			@PathVariable UUID personId,
			HttpServletRequest request) {
		List<EntityTaxonomyClientResponse> data = personTaxonomyService.list(personId);
		return ResponseEntity.ok(success("PERSON_TAXONOMIES_LISTED", "Person taxonomies retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<EntityTaxonomyClientResponse>> assign(
			@PathVariable UUID personId,
			@Valid @RequestBody PersonTaxonomyAssignRequest body,
			HttpServletRequest request) {
		EntityTaxonomyClientResponse data = personTaxonomyService.assign(personId, body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("PERSON_TAXONOMY_ASSIGNED", "Taxonomy assigned to person", data, request));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> remove(
			@PathVariable UUID personId,
			@RequestParam UUID taxonomyId,
			HttpServletRequest request) {
		personTaxonomyService.remove(personId, taxonomyId);
		return ResponseEntity.ok(success("PERSON_TAXONOMY_REMOVED", "Taxonomy removed from person", null, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
