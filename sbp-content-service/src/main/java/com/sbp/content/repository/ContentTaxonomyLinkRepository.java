package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.domain.ContentTaxonomyKind;
import com.sbp.content.entity.ContentTaxonomyLink;
import com.sbp.content.entity.ContentTaxonomyLink.ContentTaxonomyLinkId;

public interface ContentTaxonomyLinkRepository extends JpaRepository<ContentTaxonomyLink, ContentTaxonomyLinkId> {

	List<ContentTaxonomyLink> findByContentIdOrderBySortOrderAsc(UUID contentId);

	List<ContentTaxonomyLink> findByContentIdAndTaxonomyTypeOrderBySortOrderAsc(
			UUID contentId,
			ContentTaxonomyKind taxonomyType);

	void deleteByContentId(UUID contentId);

}
