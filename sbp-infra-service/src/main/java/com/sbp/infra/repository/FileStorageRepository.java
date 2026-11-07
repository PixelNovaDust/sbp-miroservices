package com.sbp.infra.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.infra.entity.FileStorageRecord;

public interface FileStorageRepository extends JpaRepository<FileStorageRecord, UUID> {

	List<FileStorageRecord> findByOwnerEntityTypeAndOwnerEntityIdOrderBySortOrderAsc(
			String ownerEntityType,
			UUID ownerEntityId);

}
