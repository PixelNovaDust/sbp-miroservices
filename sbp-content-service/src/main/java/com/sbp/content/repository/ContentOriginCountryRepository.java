package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.entity.ContentOriginCountry;
import com.sbp.content.entity.ContentOriginCountry.ContentOriginCountryId;

public interface ContentOriginCountryRepository
		extends JpaRepository<ContentOriginCountry, ContentOriginCountryId> {

	List<ContentOriginCountry> findByContentIdOrderBySortOrderAsc(UUID contentId);

	void deleteByContentId(UUID contentId);

}
