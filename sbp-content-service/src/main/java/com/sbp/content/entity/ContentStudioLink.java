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

@Entity
@Table(name = "content_studios")
@IdClass(ContentStudioLink.ContentStudioLinkId.class)
public class ContentStudioLink {

	@Id
	@Column(name = "content_id", nullable = false)
	private UUID contentId;

	@Id
	@Column(name = "studio_id", nullable = false)
	private UUID studioId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "content_id", nullable = false, insertable = false, updatable = false)
	private Content content;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public UUID getContentId() {
		return contentId;
	}

	public void setContentId(UUID contentId) {
		this.contentId = contentId;
	}

	public UUID getStudioId() {
		return studioId;
	}

	public void setStudioId(UUID studioId) {
		this.studioId = studioId;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public static class ContentStudioLinkId implements Serializable {

		private UUID contentId;

		private UUID studioId;

		public ContentStudioLinkId() {
		}

		public ContentStudioLinkId(UUID contentId, UUID studioId) {
			this.contentId = contentId;
			this.studioId = studioId;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ContentStudioLinkId that)) {
				return false;
			}
			return Objects.equals(contentId, that.contentId) && Objects.equals(studioId, that.studioId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(contentId, studioId);
		}

	}

}
