package com.sbp.content.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.content.client.PersonServiceClient;
import com.sbp.content.client.ReferenceServiceClient;
import com.sbp.content.client.StudioServiceClient;
import com.sbp.content.client.dto.PersonSummaryClientResponse;
import com.sbp.content.client.dto.StudioClientResponse;
import com.sbp.content.client.dto.TaxonomyClientResponse;
import com.sbp.content.domain.ContentTaxonomyKind;
import com.sbp.content.dto.ContentDetailResponse;
import com.sbp.content.dto.ContentDetailResponse.ContentAssetKeyResponse;
import com.sbp.content.dto.ContentDetailResponse.ContentAssetResponse;
import com.sbp.content.dto.ContentDetailResponse.ContentAuditResponse;
import com.sbp.content.dto.ContentDetailResponse.NamedRefResponse;
import com.sbp.content.entity.Content;
import com.sbp.content.entity.ContentAsset;
import com.sbp.content.entity.ContentAssetKey;
import com.sbp.content.entity.ContentCastLink;
import com.sbp.content.entity.ContentOriginCountry;
import com.sbp.content.entity.ContentStudioLink;
import com.sbp.content.entity.ContentTaxonomyLink;
import com.sbp.content.repository.ContentAssetKeyRepository;
import com.sbp.content.repository.ContentAssetRepository;
import com.sbp.content.repository.ContentCastLinkRepository;
import com.sbp.content.repository.ContentOriginCountryRepository;
import com.sbp.content.repository.ContentStudioLinkRepository;
import com.sbp.content.repository.ContentTaxonomyLinkRepository;
import com.sbp.content.util.ContentDefaults;

@Service
public class ContentAggregateService {

	private final ContentSupportService contentSupportService;

	private final ContentOriginCountryRepository originCountryRepository;

	private final ContentStudioLinkRepository studioLinkRepository;

	private final ContentCastLinkRepository castLinkRepository;

	private final ContentTaxonomyLinkRepository taxonomyLinkRepository;

	private final ContentAssetRepository assetRepository;

	private final ContentAssetKeyRepository assetKeyRepository;

	private final ReferenceServiceClient referenceServiceClient;

	private final StudioServiceClient studioServiceClient;

	private final PersonServiceClient personServiceClient;

	public ContentAggregateService(
			ContentSupportService contentSupportService,
			ContentOriginCountryRepository originCountryRepository,
			ContentStudioLinkRepository studioLinkRepository,
			ContentCastLinkRepository castLinkRepository,
			ContentTaxonomyLinkRepository taxonomyLinkRepository,
			ContentAssetRepository assetRepository,
			ContentAssetKeyRepository assetKeyRepository,
			ReferenceServiceClient referenceServiceClient,
			StudioServiceClient studioServiceClient,
			PersonServiceClient personServiceClient) {
		this.contentSupportService = contentSupportService;
		this.originCountryRepository = originCountryRepository;
		this.studioLinkRepository = studioLinkRepository;
		this.castLinkRepository = castLinkRepository;
		this.taxonomyLinkRepository = taxonomyLinkRepository;
		this.assetRepository = assetRepository;
		this.assetKeyRepository = assetKeyRepository;
		this.referenceServiceClient = referenceServiceClient;
		this.studioServiceClient = studioServiceClient;
		this.personServiceClient = personServiceClient;
	}

	@Transactional(readOnly = true)
	public ContentDetailResponse getDetail(UUID contentId) {
		Content content = contentSupportService.requireContent(contentId);
		List<String> originCountry = originCountryRepository.findByContentIdOrderBySortOrderAsc(contentId).stream()
				.map(ContentOriginCountry::getCountryCode)
				.toList();
		List<NamedRefResponse> studios = loadStudios(contentId);
		List<NamedRefResponse> casts = loadCasts(contentId);
		Map<ContentTaxonomyKind, List<NamedRefResponse>> taxonomies = loadTaxonomies(contentId);
		List<ContentAssetResponse> assets = loadAssets(contentId);
		ContentAuditResponse audit = new ContentAuditResponse(
				content.getCreatedAt(),
				content.getUpdatedAt(),
				content.getVersion());
		return new ContentDetailResponse(
				content.getId(),
				content.isAdult(),
				assets,
				audit,
				content.getBackdropPath(),
				casts,
				content.getContentRating(),
				content.getContentType(),
				content.getCreatedAt(),
				taxonomies.getOrDefault(ContentTaxonomyKind.MOOD_TAG, List.of()),
				content.getOriginalLanguage(),
				originCountry,
				content.getOverview(),
				content.getPosterPath(),
				content.getReleaseDate(),
				content.getRuntimeMinutes(),
				ContentDefaults.defaultStats(),
				content.getStatus(),
				studios,
				taxonomies.getOrDefault(ContentTaxonomyKind.TAG, List.of()),
				taxonomies.getOrDefault(ContentTaxonomyKind.GENRE, List.of()),
				content.getTagline(),
				content.getTitle(),
				content.getVisibility());
	}

	private List<NamedRefResponse> loadStudios(UUID contentId) {
		List<ContentStudioLink> links = studioLinkRepository.findByContentIdOrderBySortOrderAsc(contentId);
		List<NamedRefResponse> result = new ArrayList<>();
		for (ContentStudioLink link : links) {
			StudioClientResponse studio = studioServiceClient.getStudio(link.getStudioId());
			result.add(new NamedRefResponse(studio.id(), studio.name()));
		}
		return result;
	}

	private List<NamedRefResponse> loadCasts(UUID contentId) {
		List<ContentCastLink> links = castLinkRepository.findByContentIdOrderBySortOrderAsc(contentId);
		List<NamedRefResponse> result = new ArrayList<>();
		for (ContentCastLink link : links) {
			PersonSummaryClientResponse person = personServiceClient.getPerson(link.getPersonId());
			result.add(new NamedRefResponse(person.id(), person.name()));
		}
		return result;
	}

	private Map<ContentTaxonomyKind, List<NamedRefResponse>> loadTaxonomies(UUID contentId) {
		List<ContentTaxonomyLink> links = taxonomyLinkRepository.findByContentIdOrderBySortOrderAsc(contentId);
		if (links.isEmpty()) {
			return Map.of();
		}
		List<UUID> ids = links.stream().map(ContentTaxonomyLink::getTaxonomyId).distinct().toList();
		Map<UUID, TaxonomyClientResponse> byId = referenceServiceClient.batchTaxonomies(ids).stream()
				.collect(Collectors.toMap(TaxonomyClientResponse::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
		Map<ContentTaxonomyKind, List<NamedRefResponse>> grouped = new LinkedHashMap<>();
		for (ContentTaxonomyLink link : links) {
			TaxonomyClientResponse taxonomy = byId.get(link.getTaxonomyId());
			String name = taxonomy != null ? taxonomy.name() : link.getTaxonomyId().toString();
			UUID id = taxonomy != null ? taxonomy.id() : link.getTaxonomyId();
			grouped.computeIfAbsent(link.getTaxonomyType(), k -> new ArrayList<>())
					.add(new NamedRefResponse(id, name));
		}
		return grouped;
	}

	private List<ContentAssetResponse> loadAssets(UUID contentId) {
		List<ContentAsset> assets = assetRepository.findByContent_IdOrderBySortOrderAsc(contentId);
		List<ContentAssetResponse> result = new ArrayList<>();
		for (ContentAsset asset : assets) {
			List<ContentAssetKey> keys = assetKeyRepository.findByAsset_IdOrderBySortOrderAsc(asset.getId());
			List<ContentAssetKeyResponse> keyResponses = keys.stream()
					.map(k -> new ContentAssetKeyResponse(k.getId(), k.getStorageKey()))
					.toList();
			result.add(new ContentAssetResponse(asset.getAssetType(), keyResponses));
		}
		return result;
	}

}
