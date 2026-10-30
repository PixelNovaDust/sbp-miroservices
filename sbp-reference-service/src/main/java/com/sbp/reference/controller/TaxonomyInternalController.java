package com.sbp.reference.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.reference.dto.TaxonomyResponse;
import com.sbp.reference.service.TaxonomyService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/internal/v1/taxonomies")
public class TaxonomyInternalController {

	private final TaxonomyService taxonomyService;

	public TaxonomyInternalController(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	@GetMapping("/batch")
	public ResponseEntity<ApiResponse<List<TaxonomyResponse>>> batch(
			@RequestParam("ids") String idsParam,
			HttpServletRequest request) {
		List<UUID> ids = Arrays.stream(idsParam.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(UUID::fromString)
				.toList();
		List<TaxonomyResponse> data = taxonomyService.batchByIds(ids);
		ApiResponse<List<TaxonomyResponse>> body = ApiResponseFactory.success(
				"TAXONOMIES_BATCH_FETCHED",
				"Taxonomies retrieved",
				data,
				request.getRequestURI(),
				request.getHeader("X-Trace-Id"));
		return ResponseEntity.ok(body);
	}

}
