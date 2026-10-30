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
import com.sbp.person.dto.PersonAliasRequest;
import com.sbp.person.dto.PersonAliasResponse;
import com.sbp.person.service.PersonAliasService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{personId}/aliases")
public class PersonAliasController {

	private final PersonAliasService personAliasService;

	public PersonAliasController(PersonAliasService personAliasService) {
		this.personAliasService = personAliasService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PersonAliasResponse>>> list(
			@PathVariable UUID personId,
			HttpServletRequest request) {
		List<PersonAliasResponse> data = personAliasService.list(personId);
		return ResponseEntity.ok(success("PERSON_ALIASES_LISTED", "Aliases retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PersonAliasResponse>> create(
			@PathVariable UUID personId,
			@Valid @RequestBody PersonAliasRequest body,
			HttpServletRequest request) {
		PersonAliasResponse data = personAliasService.create(personId, body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("PERSON_ALIAS_CREATED", "Alias created", data, request));
	}

	@DeleteMapping("/{aliasId}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable UUID personId,
			@PathVariable UUID aliasId,
			HttpServletRequest request) {
		personAliasService.delete(personId, aliasId);
		return ResponseEntity.ok(success("PERSON_ALIAS_DELETED", "Alias deleted", null, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
