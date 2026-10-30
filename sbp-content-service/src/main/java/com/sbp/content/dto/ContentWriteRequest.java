package com.sbp.content.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbp.content.domain.AssetType;
import com.sbp.content.domain.ContentRating;
import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentWriteRequest(
		@NotBlank @Size(max = 500) String title,
		@Size(max = 500) String tagline,
		String overview,
		@NotNull Boolean adult,
		@NotNull ContentType contentType,
		@NotNull ContentRating contentRating,
		ContentStatus status,
		ContentVisibility visibility,
		@Size(max = 16) String originalLanguage,
		@JsonProperty("originCountry") List<@Size(min = 2, max = 2) String> originCountry,
		LocalDate releaseDate,
		Integer runtime,
		String posterPath,
		String backdropPath,
		List<UUID> studioIds,
		List<UUID> castPersonIds,
		List<UUID> tagIds,
		@JsonProperty("genreIds") List<UUID> genreIds,
		List<UUID> moodTagIds,
		@Valid List<ContentAssetInput> assets) {

	public record ContentAssetInput(
			@NotNull AssetType type,
			@Valid List<ContentAssetKeyInput> keys) {
	}

	public record ContentAssetKeyInput(@NotBlank String value) {
	}

}
