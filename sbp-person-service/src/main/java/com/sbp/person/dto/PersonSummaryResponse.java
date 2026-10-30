package com.sbp.person.dto;

import java.util.UUID;

public record PersonSummaryResponse(
		UUID id,
		String name,
		String slug,
		String stageName,
		boolean active,
		boolean verified) {
}
