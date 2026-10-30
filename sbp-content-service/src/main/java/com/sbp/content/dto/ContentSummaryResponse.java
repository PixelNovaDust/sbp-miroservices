package com.sbp.content.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.sbp.content.domain.ContentRating;
import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;

public record ContentSummaryResponse(
		UUID id,
		String title,
		ContentType contentType,
		ContentRating contentRating,
		ContentStatus status,
		ContentVisibility visibility,
		LocalDate releaseDate,
		String posterPath,
		boolean adult) {
}
