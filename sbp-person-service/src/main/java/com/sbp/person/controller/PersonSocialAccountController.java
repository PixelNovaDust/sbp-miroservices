package com.sbp.person.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.person.dto.PersonSocialAccountRequest;
import com.sbp.person.dto.PersonSocialAccountResponse;
import com.sbp.person.service.PersonSocialAccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/{personId}/social-accounts")
public class PersonSocialAccountController {

	private final PersonSocialAccountService socialAccountService;

	public PersonSocialAccountController(PersonSocialAccountService socialAccountService) {
		this.socialAccountService = socialAccountService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PersonSocialAccountResponse>>> list(
			@PathVariable UUID personId,
			HttpServletRequest request) {
		List<PersonSocialAccountResponse> data = socialAccountService.list(personId);
		return ResponseEntity.ok(success("PERSON_SOCIAL_ACCOUNTS_LISTED", "Social accounts retrieved", data, request));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PersonSocialAccountResponse>> create(
			@PathVariable UUID personId,
			@Valid @RequestBody PersonSocialAccountRequest body,
			HttpServletRequest request) {
		PersonSocialAccountResponse data = socialAccountService.create(personId, body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("PERSON_SOCIAL_ACCOUNT_CREATED", "Social account created", data, request));
	}

	@PutMapping("/{accountId}")
	public ResponseEntity<ApiResponse<PersonSocialAccountResponse>> update(
			@PathVariable UUID personId,
			@PathVariable UUID accountId,
			@Valid @RequestBody PersonSocialAccountRequest body,
			HttpServletRequest request) {
		PersonSocialAccountResponse data = socialAccountService.update(personId, accountId, body);
		return ResponseEntity.ok(success("PERSON_SOCIAL_ACCOUNT_UPDATED", "Social account updated", data, request));
	}

	@DeleteMapping("/{accountId}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable UUID personId,
			@PathVariable UUID accountId,
			HttpServletRequest request) {
		socialAccountService.delete(personId, accountId);
		return ResponseEntity.ok(success("PERSON_SOCIAL_ACCOUNT_DELETED", "Social account deleted", null, request));
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
