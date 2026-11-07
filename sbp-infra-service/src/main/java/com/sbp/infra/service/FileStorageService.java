package com.sbp.infra.service;

import lombok.RequiredArgsConstructor;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbp.common.storage.DownloadRequest;
import com.sbp.common.storage.FileLifecycleStatus;
import com.sbp.common.storage.ObjectHead;
import com.sbp.common.storage.ObjectStorageClient;
import com.sbp.common.storage.ObjectStorageKeyBuilder;
import com.sbp.common.storage.PresignUploadRequest;
import com.sbp.common.storage.PresignedUpload;
import com.sbp.common.storage.SbpStorageProperties;
import com.sbp.common.storage.StorageMimeTypes;
import com.sbp.common.storage.StorageVisibility;
import com.sbp.common.storage.UploadRequest;
import com.sbp.common.storage.UploadResult;
import com.sbp.infra.dto.DownloadUrlResponse;
import com.sbp.infra.dto.FileRegisterRequest;
import com.sbp.infra.dto.FileStorageResponse;
import com.sbp.infra.dto.PresignUploadResponse;
import com.sbp.infra.entity.FileStorageRecord;
import com.sbp.infra.exception.ResourceNotFoundException;
import com.sbp.infra.mapper.FileStorageMapper;
import com.sbp.infra.repository.FileStorageRepository;

@RequiredArgsConstructor
@Service
public class FileStorageService {

	private final FileStorageRepository repository;

	private final ObjectStorageClient objectStorage;

	private final SbpStorageProperties storageProperties;

	private final ObjectMapper objectMapper;


	@Transactional(readOnly = true)
	public FileStorageResponse get(UUID fileId) {
		return FileStorageMapper.toResponse(require(fileId), objectMapper);
	}

	@Transactional(readOnly = true)
	public List<FileStorageResponse> listByOwner(String ownerEntityType, UUID ownerEntityId) {
		return repository.findByOwnerEntityTypeAndOwnerEntityIdOrderBySortOrderAsc(ownerEntityType, ownerEntityId).stream()
				.map(row -> FileStorageMapper.toResponse(row, objectMapper))
				.toList();
	}

	@Transactional
	public FileStorageResponse upload(MultipartFile file, FileRegisterRequest request) throws java.io.IOException {
		String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
		String bucket = resolveBucket(request.bucket());
		String keyPrefix = logicalPrefix(request);
		String objectKey = ObjectStorageKeyBuilder.build(storageProperties.getKeyPrefix(), keyPrefix, filename);
		FileStorageRecord pending = newPendingRecord(request, filename, bucket, objectKey);
		repository.save(pending);

		UploadResult uploaded = objectStorage.upload(UploadRequest.builder()
				.content(file.getInputStream())
				.contentLength(file.getSize())
				.filename(filename)
				.bucket(bucket)
				.objectKey(objectKey)
				.contentType(file.getContentType())
				.build());
		applyUploadResult(pending, uploaded, request.actor());
		return FileStorageMapper.toResponse(repository.save(pending), objectMapper);
	}

	@Transactional
	public PresignUploadResponse presignUpload(String filename, long contentLength, String contentType, FileRegisterRequest request) {
		String bucket = resolveBucket(request.bucket());
		String keyPrefix = logicalPrefix(request);
		String objectKey = ObjectStorageKeyBuilder.build(storageProperties.getKeyPrefix(), keyPrefix, filename);
		FileStorageRecord pending = newPendingRecord(request, filename, bucket, objectKey);
		repository.save(pending);

		PresignedUpload presigned = objectStorage.presignUpload(PresignUploadRequest.builder()
				.filename(filename)
				.bucket(bucket)
				.objectKey(objectKey)
				.contentType(contentType)
				.contentLength(contentLength)
				.build());

		pending.setMimeType(presigned.mimeType());
		pending.setObjectReference(presigned.objectReference());
		repository.save(pending);

		return new PresignUploadResponse(
				pending.getId(),
				presigned.uploadUrl(),
				presigned.bucket(),
				presigned.objectKey(),
				presigned.mimeType(),
				presigned.expiresAt(),
				presigned.signedHeaders());
	}

	@Transactional
	public FileStorageResponse confirmUpload(UUID fileId, String actor) {
		FileStorageRecord entity = require(fileId);
		if (entity.getStatus() != FileLifecycleStatus.PENDING_UPLOAD) {
			throw new IllegalStateException("File is not pending upload: " + fileId);
		}
		ObjectHead head = objectStorage.head(entity.getBucketName(), entity.getObjectKey());
		entity.setFileSizeBytes(head.sizeBytes());
		entity.setEtag(head.etag());
		if (head.mimeType() != null && !head.mimeType().isBlank()) {
			entity.setMimeType(head.mimeType());
		}
		entity.setStatus(FileLifecycleStatus.AVAILABLE);
		entity.setUpdatedBy(actor);
		return FileStorageMapper.toResponse(repository.save(entity), objectMapper);
	}

	@Transactional(readOnly = true)
	public DownloadUrlResponse downloadUrl(UUID fileId) {
		FileStorageRecord entity = require(fileId);
		if (entity.getStatus() != FileLifecycleStatus.AVAILABLE) {
			throw new IllegalStateException("File is not available for download: " + fileId);
		}
		URL url = objectStorage.presignDownload(DownloadRequest.of(entity.getBucketName(), entity.getObjectKey()));
		Instant expiresAt = Instant.now().plus(storageProperties.getPresignDownloadTtl());
		return new DownloadUrlResponse(url, expiresAt);
	}

	@Transactional
	public void softDelete(UUID fileId, String actor) {
		FileStorageRecord entity = require(fileId);
		entity.setStatus(FileLifecycleStatus.DELETED);
		entity.setDeletedAt(Instant.now());
		entity.setUpdatedBy(actor);
		repository.save(entity);
		try {
			objectStorage.delete(entity.getBucketName(), entity.getObjectKey());
		}
		catch (RuntimeException ignored) {
			// Metadata marks deleted; object cleanup can be retried operationally.
		}
	}

	private FileStorageRecord newPendingRecord(FileRegisterRequest request, String filename, String bucket, String objectKey) {
		Instant now = Instant.now();
		FileStorageRecord entity = new FileStorageRecord();
		entity.setStorageProvider(storageProperties.getProvider());
		entity.setBucketName(bucket);
		entity.setObjectKey(objectKey);
		entity.setFileName(ObjectStorageKeyBuilder.sanitizeFilename(filename));
		entity.setOriginalFileName(filename);
		entity.setRegion(storageProperties.getRegion());
		entity.setMimeType(StorageMimeTypes.fromFilename(filename));
		entity.setStatus(FileLifecycleStatus.PENDING_UPLOAD);
		entity.setVisibility(request.visibility() != null ? request.visibility() : StorageVisibility.PRIVATE);
		entity.setOwnerService(request.ownerService().trim());
		entity.setOwnerEntityType(request.ownerEntityType().trim());
		entity.setOwnerEntityId(request.ownerEntityId());
		entity.setPurpose(request.purpose().trim());
		entity.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
		entity.setParentFileId(request.parentFileId());
		entity.setMetadata(toJsonNode(request.metadata()));
		entity.setObjectReference(ObjectStorageKeyBuilder.objectReference(
				storageProperties.getProvider().name(), bucket, objectKey));
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return entity;
	}

	private void applyUploadResult(FileStorageRecord entity, UploadResult uploaded, String actor) {
		entity.setEtag(uploaded.etag());
		entity.setFileSizeBytes(uploaded.sizeBytes() > 0 ? uploaded.sizeBytes() : entity.getFileSizeBytes());
		entity.setMimeType(uploaded.mimeType());
		entity.setObjectReference(uploaded.objectReference());
		entity.setRegion(uploaded.region());
		entity.setStatus(FileLifecycleStatus.AVAILABLE);
		entity.setUpdatedBy(actor);
	}

	private String resolveBucket(String override) {
		if (override != null && !override.isBlank()) {
			return override;
		}
		return storageProperties.getDefaultBucket();
	}

	private static String logicalPrefix(FileRegisterRequest request) {
		return request.ownerService() + "/" + request.ownerEntityType() + "/"
				+ request.ownerEntityId() + "/" + request.purpose();
	}

	private FileStorageRecord require(UUID fileId) {
		return repository.findById(fileId).orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
	}

	private JsonNode toJsonNode(Object value) {
		if (value == null) {
			return null;
		}
		return objectMapper.valueToTree(value);
	}

}
