package com.sbp.content.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentVisibility;
import com.sbp.content.dto.ContentSummaryResponse;
import com.sbp.content.dto.ContentWriteRequest;
import com.sbp.content.entity.Content;
import com.sbp.content.mapper.ContentMapper;
import com.sbp.content.repository.ContentRepository;
import com.sbp.content.spec.ContentSpecifications;

@Service
public class ContentService {

	private final ContentRepository contentRepository;

	private final ContentExternalValidator externalValidator;

	private final ContentGraphPersistence graphPersistence;

	private final ContentSupportService contentSupportService;

	public ContentService(
			ContentRepository contentRepository,
			ContentExternalValidator externalValidator,
			ContentGraphPersistence graphPersistence,
			ContentSupportService contentSupportService) {
		this.contentRepository = contentRepository;
		this.externalValidator = externalValidator;
		this.graphPersistence = graphPersistence;
		this.contentSupportService = contentSupportService;
	}

	@Transactional(readOnly = true)
	public List<ContentSummaryResponse> list(
			String title,
			com.sbp.content.domain.ContentType contentType,
			ContentStatus status,
			ContentVisibility visibility) {
		return contentRepository.findAll(ContentSpecifications.filters(title, contentType, status, visibility)).stream()
				.map(ContentMapper::toSummary)
				.toList();
	}

	@Transactional
	public Content create(ContentWriteRequest request) {
		externalValidator.validateWrite(request);
		Instant now = Instant.now();
		Content entity = new Content();
		applyScalars(entity, request);
		if (request.status() == null) {
			entity.setStatus(ContentStatus.DRAFT);
		}
		if (request.visibility() == null) {
			entity.setVisibility(ContentVisibility.PRIVATE);
		}
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		Content saved = contentRepository.save(entity);
		graphPersistence.replaceGraph(saved, request);
		return saved;
	}

	@Transactional
	public Content update(UUID contentId, ContentWriteRequest request) {
		externalValidator.validateWrite(request);
		Content entity = contentSupportService.requireContent(contentId);
		applyScalars(entity, request);
		entity.setUpdatedAt(Instant.now());
		Content saved = contentRepository.save(entity);
		graphPersistence.replaceGraph(saved, request);
		return saved;
	}

	@Transactional
	public void delete(UUID contentId) {
		Content entity = contentSupportService.requireContent(contentId);
		graphPersistence.clearGraph(contentId);
		contentRepository.delete(entity);
	}

	private void applyScalars(Content entity, ContentWriteRequest request) {
		entity.setTitle(request.title().trim());
		entity.setTagline(request.tagline());
		entity.setOverview(request.overview());
		entity.setAdult(Boolean.TRUE.equals(request.adult()));
		entity.setContentType(request.contentType());
		entity.setContentRating(request.contentRating());
		if (request.status() != null) {
			entity.setStatus(request.status());
		}
		if (request.visibility() != null) {
			entity.setVisibility(request.visibility());
		}
		entity.setOriginalLanguage(request.originalLanguage());
		entity.setReleaseDate(request.releaseDate());
		entity.setRuntimeMinutes(request.runtime());
		entity.setPosterPath(request.posterPath());
		entity.setBackdropPath(request.backdropPath());
	}

}
