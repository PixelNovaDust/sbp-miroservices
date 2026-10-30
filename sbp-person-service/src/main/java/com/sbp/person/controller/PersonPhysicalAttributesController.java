package com.sbp.person.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.person.dto.PersonPhysicalAttributesRequest;
import com.sbp.person.dto.PersonPhysicalAttributesResponse;
import com.sbp.person.service.PersonPhysicalAttributesService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{personId}/physical-attributes")
public class PersonPhysicalAttributesController {

	private final PersonPhysicalAttributesService physicalAttributesService;

	public PersonPhysicalAttributesController(PersonPhysicalAttributesService physicalAttributesService) {
		this.physicalAttributesService = physicalAttributesService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PersonPhysicalAttributesResponse>> get(
			@PathVariable UUID personId,
			HttpServletRequest request) {
		PersonPhysicalAttributesResponse data = physicalAttributesService.get(personId);
		return ResponseEntity.ok(success("PERSON_PHYSICAL_FETCHED", "Physical attributes retrieved", data, request));
	}

	@PutMapping
	public ResponseEntity<ApiResponse<PersonPhysicalAttributesResponse>> upsert(
			@PathVariable UUID personId,
			@Valid @RequestBody PersonPhysicalAttributesRequest body,
			HttpServletRequest request) {
		PersonPhysicalAttributesResponse data = physicalAttributesService.upsert(personId, body);
		return ResponseEntity.ok(success("PERSON_PHYSICAL_UPSERTED", "Physical attributes saved", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
