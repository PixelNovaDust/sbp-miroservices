package com.sbp.studio.entity;

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
@Table(name = "studios")
@Getter
@Setter
@NoArgsConstructor
public class Studio {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "studio_id", nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 150)
	private String slug;

	@Column(columnDefinition = "TEXT")
	private String website;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "logo")
	private UUID logo;

	@Column(columnDefinition = "TEXT")
	private String svg;

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
