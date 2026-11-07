package com.sbp.filestorage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.filestorage.entity.FileStorageRecord;

public interface FileStorageRepository extends JpaRepository<FileStorageRecord, UUID> {

	List<FileStorageRecord> findByOwnerEntityTypeAndOwnerEntityIdOrderBySortOrderAsc(
			String ownerEntityType,
			UUID ownerEntityId);

}
