package com.sbp.person.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonCareerHighlight;

public interface PersonCareerHighlightRepository extends JpaRepository<PersonCareerHighlight, UUID> {

	List<PersonCareerHighlight> findByPerson_IdOrderBySortOrderAsc(UUID personId);

	void deleteByPerson_Id(UUID personId);

}
