package com.sbp.person.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_studios")
@Getter
@Setter
@NoArgsConstructor
public class PersonStudio {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "person_studio_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(name = "studio_id", nullable = false)
	private UUID studioId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "relationship_type_id", nullable = false)
	private RelationshipType relationshipType;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
