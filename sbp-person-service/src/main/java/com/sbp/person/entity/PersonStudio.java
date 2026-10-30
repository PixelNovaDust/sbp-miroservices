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

@Entity
@Table(name = "person_studios")
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

	public UUID getId() {
		return id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public UUID getStudioId() {
		return studioId;
	}

	public void setStudioId(UUID studioId) {
		this.studioId = studioId;
	}

	public RelationshipType getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(RelationshipType relationshipType) {
		this.relationshipType = relationshipType;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
