package com.sbp.content.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_cast")
@IdClass(ContentCastLink.ContentCastLinkId.class)
@Getter
@Setter
@NoArgsConstructor
public class ContentCastLink {

	@Id
	@Column(name = "content_id", nullable = false)
	private UUID contentId;

	@Id
	@Column(name = "person_id", nullable = false)
	private UUID personId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "content_id", nullable = false, insertable = false, updatable = false)
	private Content content;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public static class ContentCastLinkId implements Serializable {

		private UUID contentId;

		private UUID personId;

		public ContentCastLinkId() {
		}

		public ContentCastLinkId(UUID contentId, UUID personId) {
			this.contentId = contentId;
			this.personId = personId;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ContentCastLinkId that)) {
				return false;
			}
			return Objects.equals(contentId, that.contentId) && Objects.equals(personId, that.personId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(contentId, personId);
		}

	}

}
