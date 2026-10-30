package com.sbp.person.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.BodyType;

public interface BodyTypeRepository extends JpaRepository<BodyType, UUID> {

	Optional<BodyType> findBySlug(String slug);

}
