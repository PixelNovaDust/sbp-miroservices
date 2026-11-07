package com.sbp.infra.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.sbp.common.storage.FileLifecycleStatus;
import com.sbp.common.storage.StorageProvider;
import com.sbp.common.storage.StorageVisibility;

public record FileStorageResponse(
		UUID fileId,
		StorageProvider storageProvider,
		String bucketName,
		String objectKey,
		String fileName,
		String objectReference,
		String region,
		String originalFileName,
		String mimeType,
		Long fileSizeBytes,
		String checksumSha256,
		String etag,
		FileLifecycleStatus status,
		StorageVisibility visibility,
		String ownerService,
		String ownerEntityType,
		UUID ownerEntityId,
		String purpose,
		short sortOrder,
		UUID parentFileId,
		Map<String, Object> metadata,
		Instant createdAt,
		Instant updatedAt,
		int version) {
}
