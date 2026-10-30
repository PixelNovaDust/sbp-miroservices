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
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.person.dto.PersonStudioRequest;
import com.sbp.person.dto.PersonStudioResponse;
import com.sbp.person.service.PersonStudioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{personId}/studios")
public class PersonStudioController {

	private final PersonStudioService personStudioService;

	public PersonStudioController(PersonStudioService personStudioService) {
		this.personStudioService = personStudioService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PersonStudioResponse>>> list(
			@PathVariable UUID personId,
			HttpServletRequest request) {
		List<PersonStudioResponse> data = personStudioService.list(personId);
		return ResponseEntity.ok(success("PERSON_STUDIOS_LISTED", "Person studio links retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PersonStudioResponse>> create(
			@PathVariable UUID personId,
			@Valid @RequestBody PersonStudioRequest body,
			HttpServletRequest request) {
		PersonStudioResponse data = personStudioService.create(personId, body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("PERSON_STUDIO_LINKED", "Studio linked to person", data, request));
	}

	@DeleteMapping("/{linkId}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable UUID personId,
			@PathVariable UUID linkId,
			HttpServletRequest request) {
		personStudioService.delete(personId, linkId);
		return ResponseEntity.ok(success("PERSON_STUDIO_UNLINKED", "Studio link removed", null, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
