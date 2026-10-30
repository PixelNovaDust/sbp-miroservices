package com.sbp.content.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.sbp.content.client.PersonServiceClient;
import com.sbp.content.client.ReferenceServiceClient;
import com.sbp.content.client.StudioServiceClient;
import com.sbp.content.client.dto.TaxonomyClientResponse;
import com.sbp.content.domain.ContentTaxonomyKind;
import com.sbp.content.dto.ContentWriteRequest;
import com.sbp.content.exception.BadRequestException;

@Component
public class ContentExternalValidator {

	private final StudioServiceClient studioServiceClient;

	private final PersonServiceClient personServiceClient;

	private final ReferenceServiceClient referenceServiceClient;

	public ContentExternalValidator(
			StudioServiceClient studioServiceClient,
			PersonServiceClient personServiceClient,
			ReferenceServiceClient referenceServiceClient) {
		this.studioServiceClient = studioServiceClient;
		this.personServiceClient = personServiceClient;
		this.referenceServiceClient = referenceServiceClient;
	}

	public void validateWrite(ContentWriteRequest request) {
		validateStudios(request.studioIds());
		validatePersons(request.castPersonIds());
		validateTaxonomies(request.tagIds(), ContentTaxonomyKind.TAG);
		validateTaxonomies(request.genreIds(), ContentTaxonomyKind.GENRE);
		validateTaxonomies(request.moodTagIds(), ContentTaxonomyKind.MOOD_TAG);
		ensureNoDuplicateTaxonomyIds(request);
	}

	private void validateStudios(List<UUID> studioIds) {
		if (studioIds == null) {
			return;
		}
		for (UUID studioId : studioIds) {
			try {
				var studio = studioServiceClient.getStudio(studioId);
				if (!studio.active()) {
					throw new BadRequestException("Studio is inactive: " + studioId);
				}
			}
			catch (RestClientResponseException ex) {
				throw new BadRequestException("Studio not found: " + studioId);
			}
		}
	}

	private void validatePersons(List<UUID> personIds) {
		if (personIds == null) {
			return;
		}
		for (UUID personId : personIds) {
			try {
				var person = personServiceClient.getPerson(personId);
				if (!person.active()) {
					throw new BadRequestException("Person is inactive: " + personId);
				}
			}
			catch (RestClientResponseException ex) {
				throw new BadRequestException("Person not found: " + personId);
			}
		}
	}

	private void validateTaxonomies(List<UUID> taxonomyIds, ContentTaxonomyKind expectedKind) {
		if (taxonomyIds == null || taxonomyIds.isEmpty()) {
			return;
		}
		List<TaxonomyClientResponse> resolved = referenceServiceClient.batchTaxonomies(taxonomyIds);
		Map<UUID, TaxonomyClientResponse> byId = resolved.stream()
				.collect(Collectors.toMap(TaxonomyClientResponse::id, Function.identity(), (a, b) -> a));
		for (UUID taxonomyId : taxonomyIds) {
			TaxonomyClientResponse taxonomy = byId.get(taxonomyId);
			if (taxonomy == null || !taxonomy.active()) {
				throw new BadRequestException("Taxonomy not found or inactive: " + taxonomyId);
			}
			if (!expectedKind.name().equalsIgnoreCase(taxonomy.type())) {
				throw new BadRequestException(
						"Taxonomy " + taxonomyId + " has type " + taxonomy.type() + "; expected " + expectedKind);
			}
		}
	}

	private void ensureNoDuplicateTaxonomyIds(ContentWriteRequest request) {
		Set<UUID> seen = new HashSet<>();
		addAllIds(seen, request.tagIds());
		addAllIds(seen, request.genreIds());
		addAllIds(seen, request.moodTagIds());
	}

	private void addAllIds(Set<UUID> seen, List<UUID> ids) {
		if (ids == null) {
			return;
		}
		for (UUID id : ids) {
			if (!seen.add(id)) {
				throw new BadRequestException("Duplicate taxonomy id across tag/genre/mood lists: " + id);
			}
		}
	}

}
