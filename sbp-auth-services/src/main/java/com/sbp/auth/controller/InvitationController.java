package com.sbp.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.auth.dto.InvitationCheckRequest;
import com.sbp.auth.dto.InvitationCheckResult;
import com.sbp.auth.service.InvitationService;
import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

	private final InvitationService invitationService;

	public InvitationController(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@PostMapping("/check")
	public ResponseEntity<ApiResponse<InvitationCheckResult>> check(
			@Valid @RequestBody InvitationCheckRequest body,
			HttpServletRequest request) {
		InvitationCheckResult result = invitationService.checkInvitation(body.email());
		String code = result.invitationFound() ? "INVITATION_FOUND" : "INVITATION_NOT_FOUND";
		ApiResponse<InvitationCheckResult> response = ApiResponseFactory.success(
				code,
				result.message(),
				result,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(response);
	}

}
