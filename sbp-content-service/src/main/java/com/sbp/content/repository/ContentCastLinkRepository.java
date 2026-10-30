package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.entity.ContentCastLink;
import com.sbp.content.entity.ContentCastLink.ContentCastLinkId;

public interface ContentCastLinkRepository extends JpaRepository<ContentCastLink, ContentCastLinkId> {

	List<ContentCastLink> findByContentIdOrderBySortOrderAsc(UUID contentId);

	void deleteByContentId(UUID contentId);

}
