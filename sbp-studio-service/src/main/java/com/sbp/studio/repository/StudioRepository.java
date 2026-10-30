package com.sbp.studio.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sbp.studio.entity.Studio;

public interface StudioRepository extends JpaRepository<Studio, UUID>, JpaSpecificationExecutor<Studio> {

	Optional<Studio> findByName(String name);

	Optional<Studio> findBySlug(String slug);

}
