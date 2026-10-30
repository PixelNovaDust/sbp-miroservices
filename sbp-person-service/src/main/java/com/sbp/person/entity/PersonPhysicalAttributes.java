package com.sbp.person.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "person_physical_attributes")
public class PersonPhysicalAttributes {

	@Id
	@Column(name = "person_id")
	private UUID personId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId
	@JoinColumn(name = "person_id")
	private Person person;

	@Column
	private Short bust;

	@Column
	private Short waist;

	@Column
	private Short hips;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "body_type_id")
	private BodyType bodyType;

	@Column(name = "eye_color", length = 50)
	private String eyeColor;

	@Column(name = "hair_color", length = 50)
	private String hairColor;

	@Column(nullable = false, length = 20)
	private String unit = "inches";

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public UUID getPersonId() {
		return personId;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Short getBust() {
		return bust;
	}

	public void setBust(Short bust) {
		this.bust = bust;
	}

	public Short getWaist() {
		return waist;
	}

	public void setWaist(Short waist) {
		this.waist = waist;
	}

	public Short getHips() {
		return hips;
	}

	public void setHips(Short hips) {
		this.hips = hips;
	}

	public BodyType getBodyType() {
		return bodyType;
	}

	public void setBodyType(BodyType bodyType) {
		this.bodyType = bodyType;
	}

	public String getEyeColor() {
		return eyeColor;
	}

	public void setEyeColor(String eyeColor) {
		this.eyeColor = eyeColor;
	}

	public String getHairColor() {
		return hairColor;
	}

	public void setHairColor(String hairColor) {
		this.hairColor = hairColor;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

}
