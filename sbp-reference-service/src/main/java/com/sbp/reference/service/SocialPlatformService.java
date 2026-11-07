package com.sbp.reference.service;

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
import com.sbp.reference.dto.SocialPlatformCreateRequest;
import com.sbp.reference.dto.SocialPlatformResponse;
import com.sbp.reference.dto.SocialPlatformUpdateRequest;
import com.sbp.reference.entity.SocialPlatform;
import com.sbp.reference.exception.ResourceNotFoundException;
import com.sbp.reference.mapper.ReferenceMapper;
import com.sbp.reference.repository.SocialPlatformRepository;
import com.sbp.reference.spec.ReferenceSpecifications;

@RequiredArgsConstructor
@Service
public class SocialPlatformService {

	private static final String OWNER_SERVICE = "sbp-reference-service";

	private static final String OWNER_ENTITY_TYPE = "social_platform";

	private final SocialPlatformRepository socialPlatformRepository;

	private final FileStorageServiceClient fileStorageServiceClient;


	@Transactional(readOnly = true)
	public List<SocialPlatformResponse> list(String name, Boolean active) {
		return socialPlatformRepository.findAll(ReferenceSpecifications.socialPlatformFilters(name, active)).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SocialPlatformResponse getById(UUID id) {
		return toResponse(findPlatform(id));
	}

	@Transactional(readOnly = true)
	public SocialPlatformResponse getBySlug(String slug) {
		SocialPlatform platform = socialPlatformRepository.findBySlug(slug)
				.orElseThrow(() -> new ResourceNotFoundException("Social platform not found for slug: " + slug));
		return toResponse(platform);
	}

	@Transactional
	public SocialPlatformResponse create(SocialPlatformCreateRequest request) {
		Instant now = Instant.now();
		SocialPlatform entity = new SocialPlatform();
		entity.setName(request.name().trim());
		entity.setSlug(request.slug().trim());
		entity.setBaseUrl(request.baseUrl().trim());
		applySvg(entity, request.svg(), request.svgIcon());
		entity.setBrandColor(request.brandColor());
		entity.setActive(request.active() == null || request.active());
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
		return toResponse(socialPlatformRepository.save(entity));
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
		if (request.svg() != null || request.svgIcon() != null) {
			applySvg(entity, request.svg(), request.svgIcon());
		}
		if (request.brandColor() != null) {
			entity.setBrandColor(request.brandColor());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		entity.setUpdatedBy(request.actor());
		entity.setUpdatedAt(Instant.now());
		return toResponse(socialPlatformRepository.save(entity));
	}

	@Transactional
	public SocialPlatformResponse uploadLogo(UUID id, MultipartFile file, String actor) throws java.io.IOException {
		SocialPlatform entity = findPlatform(id);
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
						id,
						"logo",
						StorageVisibility.PUBLIC,
						actor));
		entity.setLogo(fileId);
		entity.setUpdatedBy(actor);
		entity.setUpdatedAt(Instant.now());
		return toResponse(socialPlatformRepository.save(entity));
	}

	@Transactional
	public SocialPlatformResponse deactivate(UUID id, String actor) {
		SocialPlatform entity = findPlatform(id);
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		entity.setUpdatedAt(Instant.now());
		return toResponse(socialPlatformRepository.save(entity));
	}

	private SocialPlatform findPlatform(UUID id) {
		return socialPlatformRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Social platform not found: " + id));
	}

	private SocialPlatformResponse toResponse(SocialPlatform entity) {
		String logoUrl = MediaUrlSupport.resolveOrNull(fileStorageServiceClient, entity.getLogo());
		return ReferenceMapper.toSocialPlatformResponse(entity, logoUrl);
	}

	private static void applySvg(SocialPlatform entity, String svg, String svgIcon) {
		String value = svg != null ? svg : svgIcon;
		if (value != null) {
			entity.setSvg(value);
			entity.setSvgIcon(value);
		}
	}

}
