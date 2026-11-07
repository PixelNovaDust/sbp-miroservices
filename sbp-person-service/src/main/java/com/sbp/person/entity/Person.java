package com.sbp.person.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

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
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
public class Person {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "person_id", nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 150)
	private String slug;

	@Column(name = "stage_name", length = 150)
	private String stageName;

	@Column(name = "legal_name", nullable = false, length = 150)
	private String legalName;

	@Column(columnDefinition = "TEXT")
	private String bio;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "birth_place", length = 255)
	private String birthPlace;

	@Column(length = 100)
	private String nationality;

	@Column(length = 30)
	private String gender;

	@Column(name = "height_cm")
	private Short heightCm;

	@Column(name = "weight_kg")
	private Short weightKg;

	@Column(nullable = false)
	private boolean verified;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "debut_year")
	private Short debutYear;

	@Column(name = "career_status", length = 50)
	private String careerStatus;

	@Column(name = "profile_file_id")
	private UUID profileFileId;

	@Column(name = "backdrop_file_id")
	private UUID backdropFileId;

	@Column(name = "content_type", nullable = false, length = 50)
	private String contentType = "PERSON";

	@Column(name = "career_details", columnDefinition = "TEXT")
	private String careerDetails;

	@Column(name = "previous_profession", length = 255)
	private String previousProfession;

	@Column(name = "career_start_year")
	private Short careerStartYear;

	@Column(name = "retired_year")
	private Short retiredYear;

	@Column(name = "last_active_year")
	private Short lastActiveYear;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "source_metadata")
	private JsonNode sourceMetadata;

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
