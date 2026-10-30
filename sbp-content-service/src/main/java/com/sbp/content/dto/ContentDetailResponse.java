package com.sbp.content.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbp.content.domain.AssetType;
import com.sbp.content.domain.ContentRating;
import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;

public record ContentDetailResponse(
		UUID id,
		boolean adult,
		List<ContentAssetResponse> assets,
		ContentAuditResponse audit,
		String backdropPath,
		List<NamedRefResponse> casts,
		ContentRating contentRating,
		ContentType contentType,
		Instant createdAt,
		List<NamedRefResponse> moodTags,
		String originalLanguage,
		@JsonProperty("originCountry") List<String> originCountry,
		String overview,
		String posterPath,
		LocalDate releaseDate,
		Integer runtime,
		ContentStatsResponse stats,
		ContentStatus status,
		List<NamedRefResponse> studios,
		List<NamedRefResponse> tags,
		@JsonProperty("genre") List<NamedRefResponse> genre,
		String tagline,
		String title,
		ContentVisibility visibility) {

	public record NamedRefResponse(UUID id, String name) {
	}

	public record ContentAuditResponse(Instant createdAt, Instant updatedAt, int version) {
	}

	public record ContentStatsResponse(double averageRating, long totalLikes, long totalViews) {
	}

	public record ContentAssetResponse(AssetType type, List<ContentAssetKeyResponse> keys) {
	}

	public record ContentAssetKeyResponse(UUID id, String value) {
	}

}
