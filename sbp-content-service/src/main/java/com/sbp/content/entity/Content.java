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

@Entity
@Table(name = "contents")
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

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private int version;

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public boolean isAdult() {
		return adult;
	}

	public void setAdult(boolean adult) {
		this.adult = adult;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public ContentRating getContentRating() {
		return contentRating;
	}

	public void setContentRating(ContentRating contentRating) {
		this.contentRating = contentRating;
	}

	public ContentStatus getStatus() {
		return status;
	}

	public void setStatus(ContentStatus status) {
		this.status = status;
	}

	public ContentVisibility getVisibility() {
		return visibility;
	}

	public void setVisibility(ContentVisibility visibility) {
		this.visibility = visibility;
	}

	public String getOriginalLanguage() {
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage) {
		this.originalLanguage = originalLanguage;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Integer getRuntimeMinutes() {
		return runtimeMinutes;
	}

	public void setRuntimeMinutes(Integer runtimeMinutes) {
		this.runtimeMinutes = runtimeMinutes;
	}

	public String getPosterPath() {
		return posterPath;
	}

	public void setPosterPath(String posterPath) {
		this.posterPath = posterPath;
	}

	public String getBackdropPath() {
		return backdropPath;
	}

	public void setBackdropPath(String backdropPath) {
		this.backdropPath = backdropPath;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getVersion() {
		return version;
	}

}
