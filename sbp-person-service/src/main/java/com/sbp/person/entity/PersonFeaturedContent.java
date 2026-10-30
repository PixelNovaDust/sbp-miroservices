package com.sbp.person.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "person_featured_content")
public class PersonFeaturedContent {

	@EmbeddedId
	private PersonFeaturedContentId id;

	@MapsId("personId")
	@ManyToOne(optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(name = "sort_order", nullable = false)
	private short sortOrder;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public PersonFeaturedContentId getId() {
		return id;
	}

	public void setId(PersonFeaturedContentId id) {
		this.id = id;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@Embeddable
	public static class PersonFeaturedContentId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "person_id")
		private UUID personId;

		@Column(name = "content_id")
		private UUID contentId;

		public PersonFeaturedContentId() {
		}

		public PersonFeaturedContentId(UUID personId, UUID contentId) {
			this.personId = personId;
			this.contentId = contentId;
		}

		public UUID getPersonId() {
			return personId;
		}

		public void setPersonId(UUID personId) {
			this.personId = personId;
		}

		public UUID getContentId() {
			return contentId;
		}

		public void setContentId(UUID contentId) {
			this.contentId = contentId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof PersonFeaturedContentId that)) {
				return false;
			}
			return Objects.equals(personId, that.personId) && Objects.equals(contentId, that.contentId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(personId, contentId);
		}

	}

}
