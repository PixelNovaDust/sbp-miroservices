package com.sbp.person.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonAlias;

public interface PersonAliasRepository extends JpaRepository<PersonAlias, UUID> {

	List<PersonAlias> findByPerson_IdOrderByAliasAsc(UUID personId);

	void deleteByPerson_IdAndId(UUID personId, UUID id);

}
