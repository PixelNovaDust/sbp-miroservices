package com.sbp.reference.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.reference.dto.EntityTaxonomyAssignRequest;
import com.sbp.reference.dto.EntityTaxonomyResponse;
import com.sbp.reference.entity.EntityTaxonomy;
import com.sbp.reference.entity.Taxonomy;
import com.sbp.reference.exception.ResourceNotFoundException;
import com.sbp.reference.mapper.ReferenceMapper;
import com.sbp.reference.repository.EntityTaxonomyRepository;

@Service
public class EntityTaxonomyService {

	private final EntityTaxonomyRepository entityTaxonomyRepository;
	private final TaxonomyService taxonomyService;

	public EntityTaxonomyService(EntityTaxonomyRepository entityTaxonomyRepository, TaxonomyService taxonomyService) {
		this.entityTaxonomyRepository = entityTaxonomyRepository;
		this.taxonomyService = taxonomyService;
	}

	@Transactional(readOnly = true)
	public List<EntityTaxonomyResponse> listByEntity(UUID entityId, String entityType) {
		return entityTaxonomyRepository.findByEntityIdAndEntityType(entityId, entityType.trim()).stream()
				.map(ReferenceMapper::toEntityTaxonomyResponse)
				.toList();
	}

	@Transactional
	public EntityTaxonomyResponse assign(EntityTaxonomyAssignRequest request) {
		String entityType = request.entityType().trim().toUpperCase();
		return entityTaxonomyRepository
				.findByEntityIdAndEntityTypeAndTaxonomy_Id(request.entityId(), entityType, request.taxonomyId())
				.map(ReferenceMapper::toEntityTaxonomyResponse)
				.orElseGet(() -> createLink(request.entityId(), entityType, request.taxonomyId()));
	}

	@Transactional
	public void remove(UUID entityId, String entityType, UUID taxonomyId) {
		String normalizedType = entityType.trim().toUpperCase();
		if (entityTaxonomyRepository
				.findByEntityIdAndEntityTypeAndTaxonomy_Id(entityId, normalizedType, taxonomyId)
				.isEmpty()) {
			throw new ResourceNotFoundException(
					"Entity taxonomy link not found for entityId=" + entityId + ", entityType=" + normalizedType
							+ ", taxonomyId=" + taxonomyId);
		}
		entityTaxonomyRepository.deleteByEntityIdAndEntityTypeAndTaxonomy_Id(entityId, normalizedType, taxonomyId);
	}

	private EntityTaxonomyResponse createLink(UUID entityId, String entityType, UUID taxonomyId) {
		Taxonomy taxonomy = taxonomyService.findTaxonomy(taxonomyId);
		taxonomyService.ensureAssignable(taxonomy);
		EntityTaxonomy link = new EntityTaxonomy();
		link.setEntityId(entityId);
		link.setEntityType(entityType);
		link.setTaxonomy(taxonomy);
		link.setCreatedAt(Instant.now());
		return ReferenceMapper.toEntityTaxonomyResponse(entityTaxonomyRepository.save(link));
	}

}
