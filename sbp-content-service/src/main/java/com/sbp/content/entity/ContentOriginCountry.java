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
@Table(name = "content_production_countries")
@IdClass(ContentOriginCountry.ContentOriginCountryId.class)
public class ContentOriginCountry {

	@Id
	@Column(name = "content_id", nullable = false)
	private UUID contentId;

	@Id
	@Column(name = "country_code", nullable = false, length = 2)
	private String countryCode;

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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
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

	public static class ContentOriginCountryId implements Serializable {

		private UUID contentId;

		private String countryCode;

		public ContentOriginCountryId() {
		}

		public ContentOriginCountryId(UUID contentId, String countryCode) {
			this.contentId = contentId;
			this.countryCode = countryCode;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ContentOriginCountryId that)) {
				return false;
			}
			return Objects.equals(contentId, that.contentId) && Objects.equals(countryCode, that.countryCode);
		}

		@Override
		public int hashCode() {
			return Objects.hash(contentId, countryCode);
		}

	}

}
