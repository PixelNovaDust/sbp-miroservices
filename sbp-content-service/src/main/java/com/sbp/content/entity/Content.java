package com.sbp.content.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.sbp.content.domain.ContentRating;
import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@Setter
@NoArgsConstructor
public class Content {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "content_id", nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(length = 500)
	private String tagline;

	@Column(columnDefinition = "TEXT")
	private String overview;

	@Column(nullable = false)
	private boolean adult;

	@Enumerated(EnumType.STRING)
	@Column(name = "content_type", nullable = false, length = 32)
	private ContentType contentType;

	@Enumerated(EnumType.STRING)
	@Column(name = "content_rating", nullable = false, length = 64)
	private ContentRating contentRating;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ContentStatus status = ContentStatus.DRAFT;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ContentVisibility visibility = ContentVisibility.PRIVATE;

	@Column(name = "original_language", length = 16)
	private String originalLanguage;

	@Column(name = "release_date")
	private LocalDate releaseDate;

	@Column(name = "runtime_minutes")
	private Integer runtimeMinutes;

	@Column(name = "poster_path", columnDefinition = "TEXT")
	private String posterPath;

	@Column(name = "backdrop_path", columnDefinition = "TEXT")
	private String backdropPath;

	@Column(name = "poster_file_id")
	private UUID posterFileId;

	@Column(name = "backdrop_file_id")
	private UUID backdropFileId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private int version;

}
