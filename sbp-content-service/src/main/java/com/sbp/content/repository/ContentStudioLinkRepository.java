package com.sbp.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.content.entity.ContentStudioLink;
import com.sbp.content.entity.ContentStudioLink.ContentStudioLinkId;

public interface ContentStudioLinkRepository extends JpaRepository<ContentStudioLink, ContentStudioLinkId> {

	List<ContentStudioLink> findByContentIdOrderBySortOrderAsc(UUID contentId);

	void deleteByContentId(UUID contentId);

}
