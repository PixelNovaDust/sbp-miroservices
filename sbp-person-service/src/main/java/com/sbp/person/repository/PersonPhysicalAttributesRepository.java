package com.sbp.person.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonPhysicalAttributes;

public interface PersonPhysicalAttributesRepository extends JpaRepository<PersonPhysicalAttributes, UUID> {
}
