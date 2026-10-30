package com.sbp.person.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.LookupTypeCreateRequest;
import com.sbp.person.dto.LookupTypeResponse;
import com.sbp.person.entity.RelationshipType;
import com.sbp.person.repository.RelationshipTypeRepository;

@Service
public class RelationshipTypeService {

	private final RelationshipTypeRepository relationshipTypeRepository;

	public RelationshipTypeService(RelationshipTypeRepository relationshipTypeRepository) {
		this.relationshipTypeRepository = relationshipTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<LookupTypeResponse> list(Boolean active) {
		return relationshipTypeRepository.findAll().stream()
				.filter(rt -> active == null || rt.isActive() == active)
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public LookupTypeResponse getBySlug(String slug) {
		return relationshipTypeRepository.findBySlug(slug)
				.map(this::toResponse)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Relationship type not found: " + slug));
	}

	@Transactional
	public LookupTypeResponse create(LookupTypeCreateRequest request) {
		return relationshipTypeRepository.findBySlug(request.slug())
				.map(this::toResponse)
				.orElseGet(() -> toResponse(relationshipTypeRepository.save(newRelationshipType(request))));
	}

	private RelationshipType newRelationshipType(LookupTypeCreateRequest request) {
		Instant now = Instant.now();
		RelationshipType entity = new RelationshipType();
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

	private LookupTypeResponse toResponse(RelationshipType entity) {
		return new LookupTypeResponse(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getDescription(),
				entity.isActive());
	}

}
