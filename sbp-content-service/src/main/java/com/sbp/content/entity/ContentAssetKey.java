package com.sbp.content.entity;

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
@Table(name = "content_asset_keys")
@Getter
@Setter
@NoArgsConstructor
public class ContentAssetKey {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "key_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "asset_id", nullable = false)
	private ContentAsset asset;

	@Column(name = "storage_key", nullable = false, columnDefinition = "TEXT")
	private String storageKey;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "storage_file_id")
	private UUID storageFileId;

}
