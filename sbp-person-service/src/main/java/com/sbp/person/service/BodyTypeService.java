package com.sbp.person.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.LookupTypeCreateRequest;
import com.sbp.person.dto.LookupTypeResponse;
import com.sbp.person.entity.BodyType;
import com.sbp.person.repository.BodyTypeRepository;

@Service
public class BodyTypeService {

	private final BodyTypeRepository bodyTypeRepository;

	public BodyTypeService(BodyTypeRepository bodyTypeRepository) {
		this.bodyTypeRepository = bodyTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<LookupTypeResponse> list(Boolean active) {
		return bodyTypeRepository.findAll().stream()
				.filter(bt -> active == null || bt.isActive() == active)
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public LookupTypeResponse getBySlug(String slug) {
		return bodyTypeRepository.findBySlug(slug)
				.map(this::toResponse)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Body type not found: " + slug));
	}

	@Transactional
	public LookupTypeResponse create(LookupTypeCreateRequest request) {
		return bodyTypeRepository.findBySlug(request.slug())
				.map(this::toResponse)
				.orElseGet(() -> toResponse(bodyTypeRepository.save(newBodyType(request))));
	}

	private BodyType newBodyType(LookupTypeCreateRequest request) {
		Instant now = Instant.now();
		BodyType entity = new BodyType();
		entity.setName(request.name());
		entity.setSlug(request.slug());
		entity.setDescription(request.description());
		entity.setActive(true);
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return entity;
	}

	private LookupTypeResponse toResponse(BodyType entity) {
		return new LookupTypeResponse(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getDescription(),
				entity.isActive());
	}

}
