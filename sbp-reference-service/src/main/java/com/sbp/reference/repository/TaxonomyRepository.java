package com.sbp.reference.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.sbp.reference.entity.Taxonomy;

public interface TaxonomyRepository extends JpaRepository<Taxonomy, UUID>, JpaSpecificationExecutor<Taxonomy> {

	Optional<Taxonomy> findBySlug(String slug);

	List<Taxonomy> findByIdIn(List<UUID> ids);

	@Query("SELECT DISTINCT t.type FROM Taxonomy t ORDER BY t.type")
	List<String> findDistinctTypes();

}
