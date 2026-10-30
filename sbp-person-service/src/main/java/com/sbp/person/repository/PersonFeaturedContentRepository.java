package com.sbp.person.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonFeaturedContent;
import com.sbp.person.entity.PersonFeaturedContent.PersonFeaturedContentId;

public interface PersonFeaturedContentRepository extends JpaRepository<PersonFeaturedContent, PersonFeaturedContentId> {

	List<PersonFeaturedContent> findByPerson_IdOrderBySortOrderAsc(UUID personId);

}
