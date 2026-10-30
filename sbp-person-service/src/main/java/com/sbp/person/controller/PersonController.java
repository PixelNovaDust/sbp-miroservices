package com.sbp.person.controller;

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
import com.sbp.person.dto.PersonCreateRequest;
import com.sbp.person.dto.PersonPatchRequest;
import com.sbp.person.dto.PersonProfileResponse;
import com.sbp.person.dto.PersonSummaryResponse;
import com.sbp.person.dto.PersonUpdateRequest;
import com.sbp.person.entity.Person;
import com.sbp.person.service.PersonAggregateService;
import com.sbp.person.service.PersonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping
public class PersonController {

	private final PersonService personService;

	private final PersonAggregateService personAggregateService;

	public PersonController(PersonService personService, PersonAggregateService personAggregateService) {
		this.personService = personService;
		this.personAggregateService = personAggregateService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PersonSummaryResponse>>> list(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Boolean verified,
			HttpServletRequest request) {
		List<PersonSummaryResponse> data = personService.list(name, active, verified);
		return ResponseEntity.ok(success("PERSONS_LISTED", "Persons retrieved", data, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PersonProfileResponse>> getProfile(
			@PathVariable UUID id,
			HttpServletRequest request) {
		PersonProfileResponse data = personAggregateService.getProfile(id);
		return ResponseEntity.ok(success("PERSON_FETCHED", "Person profile retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PersonProfileResponse>> create(
			@Valid @RequestBody PersonCreateRequest body,
			HttpServletRequest request) {
		Person created = personService.create(body);
		PersonProfileResponse data = personAggregateService.getProfile(created.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("PERSON_CREATED", "Person created", data, request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<PersonProfileResponse>> update(
			@PathVariable UUID id,
			@Valid @RequestBody PersonUpdateRequest body,
			HttpServletRequest request) {
		personService.update(id, body);
		PersonProfileResponse data = personAggregateService.getProfile(id);
		return ResponseEntity.ok(success("PERSON_UPDATED", "Person updated", data, request));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<PersonProfileResponse>> patch(
			@PathVariable UUID id,
			@Valid @RequestBody PersonPatchRequest body,
			HttpServletRequest request) {
		personService.patch(id, body);
		PersonProfileResponse data = personAggregateService.getProfile(id);
		return ResponseEntity.ok(success("PERSON_PATCHED", "Person updated", data, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<PersonProfileResponse>> softDelete(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "system") String actor,
			HttpServletRequest request) {
		personService.softDelete(id, actor);
		PersonProfileResponse data = personAggregateService.getProfile(id);
		return ResponseEntity.ok(success("PERSON_DEACTIVATED", "Person deactivated", data, request));
	}

	@PostMapping("/{id}/restore")
	public ResponseEntity<ApiResponse<PersonProfileResponse>> restore(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "system") String actor,
			HttpServletRequest request) {
		personService.restore(id, actor);
		PersonProfileResponse data = personAggregateService.getProfile(id);
		return ResponseEntity.ok(success("PERSON_RESTORED", "Person restored", data, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
