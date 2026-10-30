package com.sbp.content.entity;

import java.util.UUID;

import com.sbp.content.domain.AssetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_assets")
public class ContentAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "asset_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "content_id", nullable = false)
	private Content content;

	@Enumerated(EnumType.STRING)
	@Column(name = "asset_type", nullable = false, length = 32)
	private AssetType assetType;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public UUID getId() {
		return id;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

}
