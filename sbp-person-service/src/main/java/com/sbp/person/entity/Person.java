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

@Entity
@Table(name = "persons")
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

	@Column(name = "profile_path", columnDefinition = "TEXT")
	private String profilePath;

	@Column(name = "backdrop_path", columnDefinition = "TEXT")
	private String backdropPath;

	@Column(name = "content_type", nullable = false, length = 50)
	private String contentType = "PERSON";

	@Column(name = "career_details", columnDefinition = "TEXT")
	private String careerDetails;

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

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public String getLegalName() {
		return legalName;
	}

	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getBirthPlace() {
		return birthPlace;
	}

	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Short getHeightCm() {
		return heightCm;
	}

	public void setHeightCm(Short heightCm) {
		this.heightCm = heightCm;
	}

	public Short getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(Short weightKg) {
		this.weightKg = weightKg;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Short getDebutYear() {
		return debutYear;
	}

	public void setDebutYear(Short debutYear) {
		this.debutYear = debutYear;
	}

	public String getCareerStatus() {
		return careerStatus;
	}

	public void setCareerStatus(String careerStatus) {
		this.careerStatus = careerStatus;
	}

	public String getProfilePath() {
		return profilePath;
	}

	public void setProfilePath(String profilePath) {
		this.profilePath = profilePath;
	}

	public String getBackdropPath() {
		return backdropPath;
	}

	public void setBackdropPath(String backdropPath) {
		this.backdropPath = backdropPath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCareerDetails() {
		return careerDetails;
	}

	public void setCareerDetails(String careerDetails) {
		this.careerDetails = careerDetails;
	}

	public Short getRetiredYear() {
		return retiredYear;
	}

	public void setRetiredYear(Short retiredYear) {
		this.retiredYear = retiredYear;
	}

	public Short getLastActiveYear() {
		return lastActiveYear;
	}

	public void setLastActiveYear(Short lastActiveYear) {
		this.lastActiveYear = lastActiveYear;
	}

	public JsonNode getSourceMetadata() {
		return sourceMetadata;
	}

	public void setSourceMetadata(JsonNode sourceMetadata) {
		this.sourceMetadata = sourceMetadata;
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
