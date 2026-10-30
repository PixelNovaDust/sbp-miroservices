package com.sbp.content.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.sbp.content.domain.ContentTaxonomyKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_taxonomies")
@IdClass(ContentTaxonomyLink.ContentTaxonomyLinkId.class)
public class ContentTaxonomyLink {

	@Id
	@Column(name = "content_id", nullable = false)
	private UUID contentId;

	@Id
	@Column(name = "taxonomy_id", nullable = false)
	private UUID taxonomyId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "content_id", nullable = false, insertable = false, updatable = false)
	private Content content;

	@Enumerated(EnumType.STRING)
	@Column(name = "taxonomy_type", nullable = false, length = 32)
	private ContentTaxonomyKind taxonomyType;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public UUID getContentId() {
		return contentId;
	}

	public void setContentId(UUID contentId) {
		this.contentId = contentId;
	}

	public UUID getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(UUID taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public ContentTaxonomyKind getTaxonomyType() {
		return taxonomyType;
	}

	public void setTaxonomyType(ContentTaxonomyKind taxonomyType) {
		this.taxonomyType = taxonomyType;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public static class ContentTaxonomyLinkId implements Serializable {

		private UUID contentId;

		private UUID taxonomyId;

		public ContentTaxonomyLinkId() {
		}

		public ContentTaxonomyLinkId(UUID contentId, UUID taxonomyId) {
			this.contentId = contentId;
			this.taxonomyId = taxonomyId;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ContentTaxonomyLinkId that)) {
				return false;
			}
			return Objects.equals(contentId, that.contentId) && Objects.equals(taxonomyId, that.taxonomyId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(contentId, taxonomyId);
		}

	}

}
