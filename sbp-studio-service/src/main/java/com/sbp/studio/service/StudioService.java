package com.sbp.studio.service;

import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sbp.common.client.FileStorageOwnerRequest;
import com.sbp.common.client.FileStorageServiceClient;
import com.sbp.common.client.MediaUrlSupport;
import com.sbp.common.storage.StorageVisibility;
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
@RequiredArgsConstructor

public class StudioService {

	private static final String OWNER_SERVICE = "sbp-studio-service";

	private static final String OWNER_ENTITY_TYPE = "studio";

	private final StudioRepository studioRepository;

	private final FileStorageServiceClient fileStorageServiceClient;


	@Transactional(readOnly = true)
	public List<StudioResponse> list(String name, Boolean active) {
		return studioRepository.findAll(StudioSpecifications.filters(name, active)).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StudioResponse getById(UUID studioId) {
		return toResponse(findStudio(studioId));
	}

	@Transactional(readOnly = true)
	public StudioResponse getByName(String name) {
		Studio studio = studioRepository.findByName(name)
				.orElseThrow(() -> new ResourceNotFoundException("Studio not found for name: " + name));
		return toResponse(studio);
	}

	@Transactional
	public StudioResponse create(StudioCreateRequest request) {
		Instant now = Instant.now();
		Studio entity = new Studio();
		entity.setName(request.name().trim());
		entity.setSlug(StudioSlugSupport.resolveForCreate(request.slug(), request.name()));
		entity.setWebsite(request.website());
		entity.setDescription(request.description());
		entity.setSvg(request.svg());
		entity.setActive(true);
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse update(UUID studioId, StudioUpdateRequest request) {
		Studio entity = findStudio(studioId);
		entity.setName(request.name().trim());
		entity.setSlug(StudioSlugSupport.normalize(request.slug()));
		entity.setWebsite(request.website());
		entity.setDescription(request.description());
		if (request.svg() != null) {
			entity.setSvg(request.svg());
		}
		entity.setUpdatedBy(request.actor());
		return toResponse(studioRepository.save(entity));
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
		if (request.svg() != null) {
			entity.setSvg(request.svg());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		entity.setUpdatedBy(request.actor());
		return toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse uploadLogo(UUID studioId, MultipartFile file, String actor) throws java.io.IOException {
		Studio entity = findStudio(studioId);
		String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo";
		ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return filename;
			}
		};
		UUID fileId = fileStorageServiceClient.upload(
				resource,
				filename,
				new FileStorageOwnerRequest(
						OWNER_SERVICE,
						OWNER_ENTITY_TYPE,
						studioId,
						"logo",
						StorageVisibility.PUBLIC,
						actor));
		entity.setLogo(fileId);
		entity.setUpdatedBy(actor);
		return toResponse(studioRepository.save(entity));
	}

	@Transactional
	public StudioResponse softDelete(UUID studioId, String actor) {
		Studio entity = findStudio(studioId);
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		return toResponse(studioRepository.save(entity));
	}

	private Studio findStudio(UUID studioId) {
		return studioRepository.findById(studioId)
				.orElseThrow(() -> new ResourceNotFoundException("Studio not found: " + studioId));
	}

	private StudioResponse toResponse(Studio entity) {
		String logoUrl = MediaUrlSupport.resolveOrNull(fileStorageServiceClient, entity.getLogo());
		return StudioMapper.toResponse(entity, logoUrl);
	}

}
