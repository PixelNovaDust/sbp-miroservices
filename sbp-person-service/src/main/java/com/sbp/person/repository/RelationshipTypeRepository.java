package com.sbp.person.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.RelationshipType;

public interface RelationshipTypeRepository extends JpaRepository<RelationshipType, UUID> {

	Optional<RelationshipType> findBySlug(String slug);

}
