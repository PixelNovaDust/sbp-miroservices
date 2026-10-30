package com.sbp.reference.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.reference.entity.EntityTaxonomy;

public interface EntityTaxonomyRepository extends JpaRepository<EntityTaxonomy, UUID> {

	List<EntityTaxonomy> findByEntityIdAndEntityType(UUID entityId, String entityType);

	Optional<EntityTaxonomy> findByEntityIdAndEntityTypeAndTaxonomy_Id(UUID entityId, String entityType,
			UUID taxonomyId);

	void deleteByEntityIdAndEntityTypeAndTaxonomy_Id(UUID entityId, String entityType, UUID taxonomyId);

}
