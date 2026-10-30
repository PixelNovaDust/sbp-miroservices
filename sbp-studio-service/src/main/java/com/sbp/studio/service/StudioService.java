package com.sbp.studio.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.studio.dto.StudioCreateRequest;
import com.sbp.studio.dto.StudioPatchRequest;
import com.sbp.studio.dto.StudioResponse;
import com.sbp.studio.dto.StudioUpdateRequest;
import com.sbp.studio.entity.Studio;
import com.sbp.studio.exception.ResourceNotFoundException;
import com.sbp.studio.mapper.StudioMapper;
import com.sbp.studio.repository.StudioRepository;
import com.sbp.studio.spec.StudioSpecifications;
import com.sbp.studio.util.StudioSlugSupport;

@Service
public class StudioService {

	private final StudioRepository studioRepository;

	public StudioService(StudioRepository studioRepository) {
		this.studioRepository = studioRepository;
	}

	@Transactional(readOnly = true)
	public List<StudioResponse> list(String name, Boolean active) {
		return studioRepository.findAll(StudioSpecifications.filters(name, active)).stream()
				.map(StudioMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StudioResponse getById(UUID studioId) {
		return StudioMapper.toResponse(findStudio(studioId));
	}

	@Transactional(readOnly = true)
	public StudioResponse getByName(String name) {
		Studio studio = studioRepository.findByName(name)
				.orElseThrow(() -> new ResourceNotFoundException("Studio not found for name: " + name));
		return StudioMapper.toResponse(studio);
	}

	@Transactional
	public StudioResponse create(StudioCreateRequest request) {
		Instant now = Instant.now();
		Studio entity = new Studio();
		entity.setName(request.name().trim());
		entity.setSlug(StudioSlugSupport.resolveForCreate(request.slug(), request.name()));
		entity.setWebsite(request.website());
		entity.setDescription(request.description());
		entity.setActive(true);
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return StudioMapper.toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse update(UUID studioId, StudioUpdateRequest request) {
		Studio entity = findStudio(studioId);
		entity.setName(request.name().trim());
		entity.setSlug(StudioSlugSupport.normalize(request.slug()));
		entity.setWebsite(request.website());
		entity.setDescription(request.description());
		entity.setUpdatedBy(request.actor());
		return StudioMapper.toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse patch(UUID studioId, StudioPatchRequest request) {
		Studio entity = findStudio(studioId);
		if (request.name() != null) {
			entity.setName(request.name().trim());
		}
		if (request.slug() != null) {
			entity.setSlug(StudioSlugSupport.normalize(request.slug()));
		}
		if (request.website() != null) {
			entity.setWebsite(request.website());
		}
		if (request.description() != null) {
			entity.setDescription(request.description());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		entity.setUpdatedBy(request.actor());
		return StudioMapper.toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse softDelete(UUID studioId, String actor) {
		Studio entity = findStudio(studioId);
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		return StudioMapper.toResponse(studioRepository.save(entity));
	}

	private Studio findStudio(UUID studioId) {
		return studioRepository.findById(studioId)
				.orElseThrow(() -> new ResourceNotFoundException("Studio not found: " + studioId));
	}

}
