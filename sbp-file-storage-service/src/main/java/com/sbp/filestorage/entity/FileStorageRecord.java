package com.sbp.filestorage.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.sbp.common.storage.FileLifecycleStatus;
import com.sbp.common.storage.StorageProvider;
import com.sbp.common.storage.StorageVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_storage")
@Getter
@Setter
@NoArgsConstructor
public class FileStorageRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "file_id", nullable = false, updatable = false)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "storage_provider", nullable = false, length = 32)
	private StorageProvider storageProvider;

	@Column(name = "bucket_name", nullable = false, length = 255)
	private String bucketName;

	@Column(name = "object_key", nullable = false, columnDefinition = "TEXT")
	private String objectKey;

	@Column(name = "file_name", length = 500)
	private String fileName;

	@Column(name = "object_reference", columnDefinition = "TEXT")
	private String objectReference;

	@Column(length = 64)
	private String region;

	@Column(name = "original_file_name", length = 500)
	private String originalFileName;

	@Column(name = "mime_type", length = 127)
	private String mimeType;

	@Column(name = "file_size_bytes")
	private Long fileSizeBytes;

	@Column(name = "checksum_sha256", length = 64)
	private String checksumSha256;

	@Column(length = 128)
	private String etag;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private FileLifecycleStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StorageVisibility visibility = StorageVisibility.PRIVATE;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "owner_service", nullable = false, length = 64)
	private String ownerService;

	@Column(name = "owner_entity_type", nullable = false, length = 32)
	private String ownerEntityType;

	@Column(name = "owner_entity_id", nullable = false)
	private UUID ownerEntityId;

	@Column(nullable = false, length = 64)
	private String purpose;

	@Column(name = "sort_order", nullable = false)
	private short sortOrder;

	@Column(name = "parent_file_id")
	private UUID parentFileId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode metadata;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false, length = 100)
	private String createdBy;

	@Column(name = "updated_by", nullable = false, length = 100)
	private String updatedBy;

	@Version
	@Column(nullable = false)
	private int version;

}
