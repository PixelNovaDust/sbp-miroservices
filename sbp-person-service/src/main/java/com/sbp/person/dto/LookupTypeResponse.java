package com.sbp.person.dto;

import java.util.UUID;

public record LookupTypeResponse(
		UUID id,
		String name,
		String slug,
		String description,
		boolean active) {
}
