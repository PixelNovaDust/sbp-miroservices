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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_platforms")
@Getter
@Setter
@NoArgsConstructor
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

	@Column(name = "logo")
	private UUID logo;

	@Column(columnDefinition = "TEXT")
	private String svg;

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

}
