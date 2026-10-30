package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.entity.ContentAssetKey;

public interface ContentAssetKeyRepository extends JpaRepository<ContentAssetKey, UUID> {

	List<ContentAssetKey> findByAsset_IdOrderBySortOrderAsc(UUID assetId);

	void deleteByAsset_IdIn(List<UUID> assetIds);

}
