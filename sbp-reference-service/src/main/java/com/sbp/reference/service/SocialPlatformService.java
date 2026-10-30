package com.sbp.reference.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.reference.dto.SocialPlatformCreateRequest;
import com.sbp.reference.dto.SocialPlatformResponse;
import com.sbp.reference.dto.SocialPlatformUpdateRequest;
import com.sbp.reference.entity.SocialPlatform;
import com.sbp.reference.exception.ResourceNotFoundException;
import com.sbp.reference.mapper.ReferenceMapper;
import com.sbp.reference.repository.SocialPlatformRepository;
import com.sbp.reference.spec.ReferenceSpecifications;

@Service
public class SocialPlatformService {

	private final SocialPlatformRepository socialPlatformRepository;

	public SocialPlatformService(SocialPlatformRepository socialPlatformRepository) {
		this.socialPlatformRepository = socialPlatformRepository;
	}

	@Transactional(readOnly = true)
	public List<SocialPlatformResponse> list(String name, Boolean active) {
		return socialPlatformRepository.findAll(ReferenceSpecifications.socialPlatformFilters(name, active)).stream()
				.map(ReferenceMapper::toSocialPlatformResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SocialPlatformResponse getById(UUID id) {
		return ReferenceMapper.toSocialPlatformResponse(findPlatform(id));
	}

	@Transactional(readOnly = true)
	public SocialPlatformResponse getBySlug(String slug) {
		SocialPlatform platform = socialPlatformRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Social platform not found for slug: " + slug));
		return ReferenceMapper.toSocialPlatformResponse(platform);
	}

	@Transactional
	public SocialPlatformResponse create(SocialPlatformCreateRequest request) {
		Instant now = Instant.now();
		SocialPlatform entity = new SocialPlatform();
		entity.setName(request.name().trim());
		entity.setSlug(request.slug().trim());
		entity.setBaseUrl(request.baseUrl().trim());
		entity.setSvgIcon(request.svgIcon());
		entity.setBrandColor(request.brandColor());
		entity.setActive(request.active() == null || request.active());
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return ReferenceMapper.toSocialPlatformResponse(socialPlatformRepository.save(entity));
	}

	@Transactional
	public SocialPlatformResponse update(UUID id, SocialPlatformUpdateRequest request) {
		SocialPlatform entity = findPlatform(id);
		if (request.name() != null) {
			entity.setName(request.name().trim());
		}
		if (request.slug() != null) {
			entity.setSlug(request.slug().trim());
		}
		if (request.baseUrl() != null) {
			entity.setBaseUrl(request.baseUrl().trim());
		}
		if (request.svgIcon() != null) {
			entity.setSvgIcon(request.svgIcon());
		}
		if (request.brandColor() != null) {
			entity.setBrandColor(request.brandColor());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		entity.setUpdatedBy(request.actor());
		entity.setUpdatedAt(Instant.now());
		return ReferenceMapper.toSocialPlatformResponse(socialPlatformRepository.save(entity));
	}

	@Transactional
	public SocialPlatformResponse deactivate(UUID id, String actor) {
		SocialPlatform entity = findPlatform(id);
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		entity.setUpdatedAt(Instant.now());
		return ReferenceMapper.toSocialPlatformResponse(socialPlatformRepository.save(entity));
	}

	private SocialPlatform findPlatform(UUID id) {
		return socialPlatformRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Social platform not found: " + id));
	}

}
