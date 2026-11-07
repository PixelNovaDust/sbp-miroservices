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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_physical_attributes")
@Getter
@Setter
@NoArgsConstructor
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

}
