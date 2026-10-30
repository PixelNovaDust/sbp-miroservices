package com.sbp.person.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbp.person.client.ReferenceServiceClient;
import com.sbp.person.client.StudioServiceClient;
import com.sbp.person.client.dto.EntityTaxonomyClientResponse;
import com.sbp.person.client.dto.TaxonomyClientResponse;
import com.sbp.person.dto.PersonProfileResponse;
import com.sbp.person.dto.PersonProfileResponse.Audit;
import com.sbp.person.dto.PersonProfileResponse.Career;
import com.sbp.person.dto.PersonProfileResponse.FeaturedContentItem;
import com.sbp.person.dto.PersonProfileResponse.Measurements;
import com.sbp.person.dto.PersonProfileResponse.SocialPresence;
import com.sbp.person.dto.PersonProfileResponse.TaxonomyItem;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonCareerHighlight;
import com.sbp.person.entity.PersonFeaturedContent;
import com.sbp.person.entity.PersonPhysicalAttributes;
import com.sbp.person.entity.PersonSocialAccount;
import com.sbp.person.entity.PersonStudio;
import com.sbp.person.repository.PersonAliasRepository;
import com.sbp.person.repository.PersonCareerHighlightRepository;
import com.sbp.person.repository.PersonFeaturedContentRepository;
import com.sbp.person.repository.PersonPhysicalAttributesRepository;
import com.sbp.person.repository.PersonSocialAccountRepository;
import com.sbp.person.repository.PersonStudioRepository;
import com.sbp.person.util.GenderDisplay;

@Service
public class PersonAggregateService {

	private static final Logger log = LoggerFactory.getLogger(PersonAggregateService.class);

	private final PersonSupportService personSupportService;

	private final PersonAliasRepository personAliasRepository;

	private final PersonPhysicalAttributesRepository physicalAttributesRepository;

	private final PersonSocialAccountRepository socialAccountRepository;

	private final PersonStudioRepository personStudioRepository;

	private final PersonCareerHighlightRepository careerHighlightRepository;

	private final PersonFeaturedContentRepository featuredContentRepository;

	private final ReferenceServiceClient referenceServiceClient;

	private final StudioServiceClient studioServiceClient;

	private final ObjectMapper objectMapper;

	public PersonAggregateService(
			PersonSupportService personSupportService,
			PersonAliasRepository personAliasRepository,
			PersonPhysicalAttributesRepository physicalAttributesRepository,
			PersonSocialAccountRepository socialAccountRepository,
			PersonStudioRepository personStudioRepository,
			PersonCareerHighlightRepository careerHighlightRepository,
			PersonFeaturedContentRepository featuredContentRepository,
			ReferenceServiceClient referenceServiceClient,
			StudioServiceClient studioServiceClient,
			ObjectMapper objectMapper) {
		this.personSupportService = personSupportService;
		this.personAliasRepository = personAliasRepository;
		this.physicalAttributesRepository = physicalAttributesRepository;
		this.socialAccountRepository = socialAccountRepository;
		this.personStudioRepository = personStudioRepository;
		this.careerHighlightRepository = careerHighlightRepository;
		this.featuredContentRepository = featuredContentRepository;
		this.referenceServiceClient = referenceServiceClient;
		this.studioServiceClient = studioServiceClient;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public PersonProfileResponse getProfile(UUID personId) {
		Person person = personSupportService.requirePerson(personId);
		List<String> aliases = personAliasRepository.findByPerson_IdOrderByAliasAsc(personId).stream()
				.map(a -> a.getAlias())
				.toList();
		Measurements measurements = physicalAttributesRepository.findById(personId)
				.map(this::toMeasurements)
				.orElse(null);
		List<SocialPresence> socialPresence = buildSocialPresence(personId);
		TaxonomyBuckets taxonomies = loadTaxonomies(personId);
		List<String> knownFor = loadKnownForStudios(personId);
		List<String> highlights = loadCareerHighlights(personId);
		List<FeaturedContentItem> featuredContent = loadFeaturedContent(personId);
		Career career = new Career(person.getCareerDetails(), highlights, knownFor, featuredContent);
		Map<String, Object> stats = buildStats(personId, taxonomies, socialPresence, featuredContent);
		return new PersonProfileResponse(
				person.getId(),
				person.isActive(),
				person.getName(),
				person.getStageName(),
				person.getLegalName(),
				person.getBio(),
				person.getBirthDate(),
				person.getBirthPlace(),
				person.getNationality(),
				person.getGender(),
				GenderDisplay.toDisplay(person.getGender()),
				person.getHeightCm() != null ? person.getHeightCm().intValue() : null,
				person.getWeightKg() != null ? person.getWeightKg().intValue() : null,
				person.isVerified(),
				person.getDebutYear() != null ? person.getDebutYear().intValue() : null,
				person.getCareerStatus(),
				person.getRetiredYear() != null ? person.getRetiredYear().intValue() : null,
				person.getLastActiveYear() != null ? person.getLastActiveYear().intValue() : null,
				person.getProfilePath(),
				person.getBackdropPath(),
				person.getContentType(),
				aliases,
				measurements,
				taxonomies.categories(),
				taxonomies.tags(),
				taxonomies.specialties(),
				taxonomies.roles(),
				career,
				socialPresence,
				toSourceMetadataMap(person.getSourceMetadata()),
				stats,
				new Audit(person.getCreatedAt(), person.getUpdatedAt(), person.getVersion()));
	}

	private Measurements toMeasurements(PersonPhysicalAttributes attrs) {
		var bodyType = attrs.getBodyType();
		return new Measurements(
				toInt(attrs.getBust()),
				toInt(attrs.getWaist()),
				toInt(attrs.getHips()),
				attrs.getUnit(),
				attrs.getEyeColor(),
				attrs.getHairColor(),
				bodyType != null ? bodyType.getId() : null,
				bodyType != null ? bodyType.getName() : null);
	}

	private Integer toInt(Short value) {
		return value != null ? value.intValue() : null;
	}

	private List<SocialPresence> buildSocialPresence(UUID personId) {
		List<PersonSocialAccount> accounts = socialAccountRepository.findByPerson_IdOrderByUsernameAsc(personId);
		List<SocialPresence> result = new ArrayList<>();
		for (PersonSocialAccount account : accounts) {
			String platformSlug = null;
			try {
				platformSlug = referenceServiceClient.getSocialPlatform(account.getSocialPlatformId()).slug();
			}
			catch (RuntimeException ex) {
				log.debug("Could not resolve social platform {}: {}", account.getSocialPlatformId(), ex.getMessage());
			}
			result.add(new SocialPresence(
					account.getSocialPlatformId(),
					platformSlug,
					account.getUsername(),
					account.isVerified(),
					account.isAvailable()));
		}
		return result;
	}

	private TaxonomyBuckets loadTaxonomies(UUID personId) {
		try {
			List<EntityTaxonomyClientResponse> links = referenceServiceClient.listEntityTaxonomies(personId);
			if (links.isEmpty()) {
				return TaxonomyBuckets.empty();
			}
			List<UUID> taxonomyIds = links.stream().map(EntityTaxonomyClientResponse::taxonomyId).toList();
			Map<UUID, TaxonomyClientResponse> byId = new HashMap<>();
			for (TaxonomyClientResponse taxonomy : referenceServiceClient.batchTaxonomies(taxonomyIds)) {
				byId.put(taxonomy.id(), taxonomy);
			}
			List<TaxonomyItem> categories = new ArrayList<>();
			List<TaxonomyItem> tags = new ArrayList<>();
			List<TaxonomyItem> specialties = new ArrayList<>();
			List<TaxonomyItem> roles = new ArrayList<>();
			for (EntityTaxonomyClientResponse link : links) {
				TaxonomyClientResponse taxonomy = byId.get(link.taxonomyId());
				if (taxonomy == null) {
					continue;
				}
				TaxonomyItem item = new TaxonomyItem(taxonomy.id(), taxonomy.name(), taxonomy.slug(), taxonomy.type());
				String type = taxonomy.type() != null ? taxonomy.type().trim().toUpperCase() : "";
				switch (type) {
					case "CATEGORY", "GENRE" -> categories.add(item);
					case "TAG", "MOOD_TAG" -> tags.add(item);
					case "SPECIALTIES", "SPECIALTY" -> specialties.add(item);
					case "ROLE" -> roles.add(item);
					default -> {
					}
				}
			}
			return new TaxonomyBuckets(categories, tags, specialties, roles);
		}
		catch (RuntimeException ex) {
			log.debug("Could not load taxonomies for person {}: {}", personId, ex.getMessage());
			return TaxonomyBuckets.empty();
		}
	}

	private List<String> loadKnownForStudios(UUID personId) {
		List<PersonStudio> links = personStudioRepository.findByPerson_IdOrderByCreatedAtAsc(personId);
		List<String> names = new ArrayList<>();
		for (PersonStudio link : links) {
			try {
				names.add(studioServiceClient.getStudio(link.getStudioId()).name());
			}
			catch (RuntimeException ex) {
				log.debug("Could not resolve studio {}: {}", link.getStudioId(), ex.getMessage());
			}
		}
		return names;
	}

	private List<String> loadCareerHighlights(UUID personId) {
		return careerHighlightRepository.findByPerson_IdOrderBySortOrderAsc(personId).stream()
				.map(PersonCareerHighlight::getText)
				.toList();
	}

	private List<FeaturedContentItem> loadFeaturedContent(UUID personId) {
		List<PersonFeaturedContent> links = featuredContentRepository.findByPerson_IdOrderBySortOrderAsc(personId);
		List<FeaturedContentItem> result = new ArrayList<>();
		for (PersonFeaturedContent link : links) {
			UUID contentId = link.getId().getContentId();
			result.add(new FeaturedContentItem(contentId, null, null));
		}
		return result;
	}

	private Map<String, Object> buildStats(
			UUID personId,
			TaxonomyBuckets taxonomies,
			List<SocialPresence> socialPresence,
			List<FeaturedContentItem> featuredContent) {
		Map<String, Object> stats = new HashMap<>();
		stats.put("studioLinkCount", personStudioRepository.findByPerson_IdOrderByCreatedAtAsc(personId).size());
		stats.put("socialAccountCount", socialPresence.size());
		stats.put("featuredContentCount", featuredContent.size());
		stats.put(
				"taxonomyAssignmentCount",
				taxonomies.categories().size()
						+ taxonomies.tags().size()
						+ taxonomies.specialties().size()
						+ taxonomies.roles().size());
		return stats;
	}

	private Map<String, Object> toSourceMetadataMap(JsonNode sourceMetadata) {
		if (sourceMetadata == null || sourceMetadata.isNull()) {
			return null;
		}
		return objectMapper.convertValue(sourceMetadata, new TypeReference<Map<String, Object>>() {
		});
	}

	private record TaxonomyBuckets(
			List<TaxonomyItem> categories,
			List<TaxonomyItem> tags,
			List<TaxonomyItem> specialties,
			List<TaxonomyItem> roles) {

		static TaxonomyBuckets empty() {
			return new TaxonomyBuckets(List.of(), List.of(), List.of(), List.of());
		}

	}

}
