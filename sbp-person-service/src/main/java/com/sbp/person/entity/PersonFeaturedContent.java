package com.sbp.person.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "person_featured_content")
@Getter
@Setter
@NoArgsConstructor
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

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	public static class PersonFeaturedContentId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "person_id")
		private UUID personId;

		@Column(name = "content_id")
		private UUID contentId;

	}

}
