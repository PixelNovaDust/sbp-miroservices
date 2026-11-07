package com.sbp.infra.mapper;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbp.infra.dto.FileStorageResponse;
import com.sbp.infra.entity.FileStorageRecord;

public final class FileStorageMapper {

	private FileStorageMapper() {
	}

	public static FileStorageResponse toResponse(FileStorageRecord entity, ObjectMapper objectMapper) {
		return new FileStorageResponse(
				entity.getId(),
				entity.getStorageProvider(),
				entity.getBucketName(),
				entity.getObjectKey(),
				entity.getFileName(),
				entity.getObjectReference(),
				entity.getRegion(),
				entity.getOriginalFileName(),
				entity.getMimeType(),
				entity.getFileSizeBytes(),
				entity.getChecksumSha256(),
				entity.getEtag(),
				entity.getStatus(),
				entity.getVisibility(),
				entity.getOwnerService(),
				entity.getOwnerEntityType(),
				entity.getOwnerEntityId(),
				entity.getPurpose(),
				entity.getSortOrder(),
				entity.getParentFileId(),
				toMetadataMap(entity.getMetadata(), objectMapper),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getVersion());
	}

	private static Map<String, Object> toMetadataMap(JsonNode metadata, ObjectMapper objectMapper) {
		if (metadata == null || metadata.isNull()) {
			return null;
		}
		return objectMapper.convertValue(metadata, Map.class);
	}

}
