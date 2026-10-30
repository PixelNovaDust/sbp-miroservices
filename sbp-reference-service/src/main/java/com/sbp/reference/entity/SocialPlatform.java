package com.sbp.reference.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "social_platforms")
public class SocialPlatform {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "social_platform_id", nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 100)
	private String slug;

	@Column(name = "base_url", nullable = false, length = 255)
	private String baseUrl;

	@Column(name = "svg_icon", columnDefinition = "TEXT")
	private String svgIcon;

	@Column(name = "brand_color", length = 20)
	private String brandColor;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false, length = 100)
	private String createdBy;

	@Column(name = "updated_by", nullable = false, length = 100)
	private String updatedBy;

	@Version
	@Column(nullable = false)
	private int version;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSvgIcon() {
		return svgIcon;
	}

	public void setSvgIcon(String svgIcon) {
		this.svgIcon = svgIcon;
	}

	public String getBrandColor() {
		return brandColor;
	}

	public void setBrandColor(String brandColor) {
		this.brandColor = brandColor;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public int getVersion() {
		return version;
	}

}
