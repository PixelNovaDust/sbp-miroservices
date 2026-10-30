package com.sbp.reference.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.reference.dto.TaxonomyCreateRequest;
import com.sbp.reference.dto.TaxonomyResponse;
import com.sbp.reference.dto.TaxonomyUpdateRequest;
import com.sbp.reference.entity.Taxonomy;
import com.sbp.reference.exception.InvalidTaxonomyTypeException;
import com.sbp.reference.exception.ResourceNotFoundException;
import com.sbp.reference.mapper.ReferenceMapper;
import com.sbp.reference.repository.TaxonomyRepository;
import com.sbp.reference.repository.TaxonomyTypeRepository;
import com.sbp.reference.spec.ReferenceSpecifications;

@Service
public class TaxonomyService {

	private final TaxonomyRepository taxonomyRepository;
	private final TaxonomyTypeRepository taxonomyTypeRepository;

	public TaxonomyService(TaxonomyRepository taxonomyRepository, TaxonomyTypeRepository taxonomyTypeRepository) {
		this.taxonomyRepository = taxonomyRepository;
		this.taxonomyTypeRepository = taxonomyTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<TaxonomyResponse> list(String type, String name, Boolean active) {
		return taxonomyRepository.findAll(ReferenceSpecifications.taxonomyFilters(type, name, active)).stream()
				.map(ReferenceMapper::toTaxonomyResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TaxonomyResponse getById(UUID id) {
		return ReferenceMapper.toTaxonomyResponse(findTaxonomy(id));
	}

	@Transactional(readOnly = true)
	public TaxonomyResponse getBySlug(String slug) {
		Taxonomy taxonomy = taxonomyRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Taxonomy not found for slug: " + slug));
		return ReferenceMapper.toTaxonomyResponse(taxonomy);
	}

	@Transactional(readOnly = true)
	public List<String> listTypes() {
		return taxonomyTypeRepository.findByActiveTrueOrderBySortOrderAscCodeAsc().stream()
				.map(type -> type.getCode())
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TaxonomyResponse> batchByIds(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return taxonomyRepository.findByIdIn(ids).stream()
				.map(ReferenceMapper::toTaxonomyResponse)
				.toList();
	}

	@Transactional
	public TaxonomyResponse create(TaxonomyCreateRequest request) {
		String type = TaxonomyTypeSupport.normalizeType(request.type());
		TaxonomyTypeSupport.assertAllowedType(taxonomyTypeRepository, type);
		Instant now = Instant.now();
		Taxonomy entity = new Taxonomy();
		entity.setType(type);
		entity.setName(request.name().trim());
		entity.setSlug(request.slug().trim());
		entity.setDescription(request.description());
		entity.setActive(request.active() == null || request.active());
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return ReferenceMapper.toTaxonomyResponse(taxonomyRepository.save(entity));
	}

	@Transactional
	public TaxonomyResponse update(UUID id, TaxonomyUpdateRequest request) {
		Taxonomy entity = findTaxonomy(id);
		if (request.type() != null) {
			String type = TaxonomyTypeSupport.normalizeType(request.type());
			TaxonomyTypeSupport.assertAllowedType(taxonomyTypeRepository, type);
			entity.setType(type);
		}
		if (request.name() != null) {
			entity.setName(request.name().trim());
		}
		if (request.slug() != null) {
			entity.setSlug(request.slug().trim());
		}
		if (request.description() != null) {
			entity.setDescription(request.description());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		entity.setUpdatedBy(request.actor());
		entity.setUpdatedAt(Instant.now());
		return ReferenceMapper.toTaxonomyResponse(taxonomyRepository.save(entity));
	}

	@Transactional
	public TaxonomyResponse deactivate(UUID id, String actor) {
		Taxonomy entity = findTaxonomy(id);
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		entity.setUpdatedAt(Instant.now());
		return ReferenceMapper.toTaxonomyResponse(taxonomyRepository.save(entity));
	}

	Taxonomy findTaxonomy(UUID id) {
		return taxonomyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Taxonomy not found: " + id));
	}

	void ensureAssignable(Taxonomy taxonomy) {
		TaxonomyTypeSupport.assertAllowedType(taxonomyTypeRepository, taxonomy.getType());
		if (!taxonomy.isActive()) {
			throw new InvalidTaxonomyTypeException("Cannot assign inactive taxonomy: " + taxonomy.getId());
		}
	}

}
