package com.sbp.content.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sbp.content.domain.ContentTaxonomyKind;
import com.sbp.content.dto.ContentWriteRequest;
import com.sbp.content.dto.ContentWriteRequest.ContentAssetInput;
import com.sbp.content.dto.ContentWriteRequest.ContentAssetKeyInput;
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

@Component
public class ContentGraphPersistence {

	private final ContentOriginCountryRepository originCountryRepository;

	private final ContentStudioLinkRepository studioLinkRepository;

	private final ContentCastLinkRepository castLinkRepository;

	private final ContentTaxonomyLinkRepository taxonomyLinkRepository;

	private final ContentAssetRepository assetRepository;

	private final ContentAssetKeyRepository assetKeyRepository;

	public ContentGraphPersistence(
			ContentOriginCountryRepository originCountryRepository,
			ContentStudioLinkRepository studioLinkRepository,
			ContentCastLinkRepository castLinkRepository,
			ContentTaxonomyLinkRepository taxonomyLinkRepository,
			ContentAssetRepository assetRepository,
			ContentAssetKeyRepository assetKeyRepository) {
		this.originCountryRepository = originCountryRepository;
		this.studioLinkRepository = studioLinkRepository;
		this.castLinkRepository = castLinkRepository;
		this.taxonomyLinkRepository = taxonomyLinkRepository;
		this.assetRepository = assetRepository;
		this.assetKeyRepository = assetKeyRepository;
	}

	public void replaceGraph(Content content, ContentWriteRequest request) {
		UUID contentId = content.getId();
		clearGraph(contentId);
		saveOriginCountries(content, request.originCountry());
		saveStudioLinks(content, request.studioIds());
		saveCastLinks(content, request.castPersonIds());
		saveTaxonomyLinks(content, request.tagIds(), ContentTaxonomyKind.TAG);
		saveTaxonomyLinks(content, request.genreIds(), ContentTaxonomyKind.GENRE);
		saveTaxonomyLinks(content, request.moodTagIds(), ContentTaxonomyKind.MOOD_TAG);
		saveAssets(content, request.assets());
	}

	public void clearGraph(UUID contentId) {
		originCountryRepository.deleteByContentId(contentId);
		studioLinkRepository.deleteByContentId(contentId);
		castLinkRepository.deleteByContentId(contentId);
		taxonomyLinkRepository.deleteByContentId(contentId);
		List<ContentAsset> assets = assetRepository.findByContent_IdOrderBySortOrderAsc(contentId);
		if (!assets.isEmpty()) {
			List<UUID> assetIds = assets.stream().map(ContentAsset::getId).toList();
			assetKeyRepository.deleteByAsset_IdIn(assetIds);
			assetRepository.deleteAll(assets);
		}
	}

	private void saveOriginCountries(Content content, List<String> countryCodes) {
		if (countryCodes == null) {
			return;
		}
		int order = 0;
		for (String raw : countryCodes) {
			if (raw == null || raw.isBlank()) {
				continue;
			}
			ContentOriginCountry row = new ContentOriginCountry();
			row.setContentId(content.getId());
			row.setContent(content);
			row.setCountryCode(raw.trim().toUpperCase(Locale.ROOT));
			row.setSortOrder(order++);
			originCountryRepository.save(row);
		}
	}

	private void saveStudioLinks(Content content, List<UUID> studioIds) {
		if (studioIds == null) {
			return;
		}
		int order = 0;
		for (UUID studioId : studioIds) {
			ContentStudioLink link = new ContentStudioLink();
			link.setContentId(content.getId());
			link.setContent(content);
			link.setStudioId(studioId);
			link.setSortOrder(order++);
			studioLinkRepository.save(link);
		}
	}

	private void saveCastLinks(Content content, List<UUID> personIds) {
		if (personIds == null) {
			return;
		}
		int order = 0;
		for (UUID personId : personIds) {
			ContentCastLink link = new ContentCastLink();
			link.setContentId(content.getId());
			link.setContent(content);
			link.setPersonId(personId);
			link.setSortOrder(order++);
			castLinkRepository.save(link);
		}
	}

	private void saveTaxonomyLinks(Content content, List<UUID> taxonomyIds, ContentTaxonomyKind kind) {
		if (taxonomyIds == null) {
			return;
		}
		int order = 0;
		for (UUID taxonomyId : taxonomyIds) {
			ContentTaxonomyLink link = new ContentTaxonomyLink();
			link.setContentId(content.getId());
			link.setContent(content);
			link.setTaxonomyId(taxonomyId);
			link.setTaxonomyType(kind);
			link.setSortOrder(order++);
			taxonomyLinkRepository.save(link);
		}
	}

	private void saveAssets(Content content, List<ContentAssetInput> assets) {
		if (assets == null) {
			return;
		}
		int assetOrder = 0;
		for (ContentAssetInput assetInput : assets) {
			ContentAsset asset = new ContentAsset();
			asset.setContent(content);
			asset.setAssetType(assetInput.type());
			asset.setSortOrder(assetOrder++);
			ContentAsset savedAsset = assetRepository.save(asset);
			saveAssetKeys(savedAsset, assetInput.keys());
		}
	}

	private void saveAssetKeys(ContentAsset asset, List<ContentAssetKeyInput> keys) {
		if (keys == null) {
			return;
		}
		int order = 0;
		for (ContentAssetKeyInput keyInput : keys) {
			if (keyInput == null || keyInput.value() == null || keyInput.value().isBlank()) {
				continue;
			}
			ContentAssetKey key = new ContentAssetKey();
			key.setAsset(asset);
			key.setStorageKey(keyInput.value().trim());
			key.setSortOrder(order++);
			assetKeyRepository.save(key);
		}
	}

}
