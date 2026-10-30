package com.sbp.person.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "person_career_highlights")
public class PersonCareerHighlight {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "highlight_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(name = "sort_order", nullable = false)
	private short sortOrder;

	@Column(name = "highlight_text", nullable = false, columnDefinition = "TEXT")
	private String text;

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

	public short getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(short sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
