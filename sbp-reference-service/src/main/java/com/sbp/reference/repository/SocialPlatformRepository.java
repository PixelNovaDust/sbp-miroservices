package com.sbp.reference.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sbp.reference.entity.SocialPlatform;

public interface SocialPlatformRepository
		extends JpaRepository<SocialPlatform, UUID>, JpaSpecificationExecutor<SocialPlatform> {

	Optional<SocialPlatform> findBySlug(String slug);

}
