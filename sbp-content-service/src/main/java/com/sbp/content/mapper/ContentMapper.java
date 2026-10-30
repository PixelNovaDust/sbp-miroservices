package com.sbp.content.mapper;

import com.sbp.content.dto.ContentSummaryResponse;
import com.sbp.content.entity.Content;

public final class ContentMapper {

	private ContentMapper() {
	}

	public static ContentSummaryResponse toSummary(Content entity) {
		return new ContentSummaryResponse(
				entity.getId(),
				entity.getTitle(),
				entity.getContentType(),
				entity.getContentRating(),
				entity.getStatus(),
				entity.getVisibility(),
				entity.getReleaseDate(),
				entity.getPosterPath(),
				entity.isAdult());
	}

}
