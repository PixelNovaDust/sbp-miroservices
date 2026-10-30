package com.sbp.person.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonStudio;

public interface PersonStudioRepository extends JpaRepository<PersonStudio, UUID> {

	List<PersonStudio> findByPerson_IdOrderByCreatedAtAsc(UUID personId);

	void deleteByPerson_IdAndId(UUID personId, UUID id);

}
