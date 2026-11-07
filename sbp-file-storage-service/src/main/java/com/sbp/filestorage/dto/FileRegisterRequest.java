package com.sbp.filestorage.dto;

import java.util.Map;
import java.util.UUID;

import com.sbp.common.storage.StorageVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FileRegisterRequest(
		@NotBlank @Size(max = 64) String ownerService,
		@NotBlank @Size(max = 32) String ownerEntityType,
		@NotNull UUID ownerEntityId,
		@NotBlank @Size(max = 64) String purpose,
		StorageVisibility visibility,
		@Size(max = 255) String bucket,
		Short sortOrder,
		UUID parentFileId,
		Map<String, Object> metadata,
		@NotBlank @Size(max = 100) String actor) {
}
