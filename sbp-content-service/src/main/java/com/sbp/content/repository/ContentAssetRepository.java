package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.entity.ContentAsset;

public interface ContentAssetRepository extends JpaRepository<ContentAsset, UUID> {

	List<ContentAsset> findByContent_IdOrderBySortOrderAsc(UUID contentId);

	void deleteByContent_Id(UUID contentId);

}
