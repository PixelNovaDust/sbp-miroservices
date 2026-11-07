package com.sbp.common.client;

import java.util.UUID;

import com.sbp.common.storage.StorageVisibility;

public record FileStorageOwnerRequest(
		String ownerService,
		String ownerEntityType,
		UUID ownerEntityId,
		String purpose,
		StorageVisibility visibility,
		String actor) {

	public FileStorageOwnerRequest {
		if (ownerService == null || ownerService.isBlank()) {
			throw new IllegalArgumentException("ownerService is required");
		}
		if (ownerEntityType == null || ownerEntityType.isBlank()) {
			throw new IllegalArgumentException("ownerEntityType is required");
		}
		if (ownerEntityId == null) {
			throw new IllegalArgumentException("ownerEntityId is required");
		}
		if (purpose == null || purpose.isBlank()) {
			throw new IllegalArgumentException("purpose is required");
		}
		if (actor == null || actor.isBlank()) {
			throw new IllegalArgumentException("actor is required");
		}
	}
}
