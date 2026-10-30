package com.sbp.person.client;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sbp.common.client.ApiClientSupport;
import com.sbp.common.response.ApiResponse;
import com.sbp.person.client.dto.EntityTaxonomyAssignClientRequest;
import com.sbp.person.client.dto.EntityTaxonomyClientResponse;
import com.sbp.person.client.dto.SocialPlatformClientResponse;
import com.sbp.person.client.dto.TaxonomyClientResponse;
import com.sbp.person.config.ClientProperties;

@Component
public class ReferenceServiceClient {

	private static final String ENTITY_TYPE_PERSON = "PERSON";

	private static final ParameterizedTypeReference<ApiResponse<List<EntityTaxonomyClientResponse>>> ENTITY_TAXONOMY_LIST_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private static final ParameterizedTypeReference<ApiResponse<List<TaxonomyClientResponse>>> TAXONOMY_BATCH_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private static final ParameterizedTypeReference<ApiResponse<SocialPlatformClientResponse>> SOCIAL_PLATFORM_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private static final ParameterizedTypeReference<ApiResponse<EntityTaxonomyClientResponse>> ENTITY_TAXONOMY_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;

	public ReferenceServiceClient(RestClient.Builder restClientBuilder, ClientProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getReferenceBaseUrl()).build();
	}

	public List<EntityTaxonomyClientResponse> listEntityTaxonomies(UUID entityId) {
		ApiResponse<List<EntityTaxonomyClientResponse>> response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/internal/v1/entity-taxonomies")
						.queryParam("entityId", entityId)
						.queryParam("entityType", ENTITY_TYPE_PERSON)
						.build())
				.retrieve()
				.body(ENTITY_TAXONOMY_LIST_TYPE);
		List<EntityTaxonomyClientResponse> data = ApiClientSupport.requireData(response);
		return data != null ? data : List.of();
	}

	public List<TaxonomyClientResponse> batchTaxonomies(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		String idsParam = ids.stream().map(UUID::toString).collect(Collectors.joining(","));
		ApiResponse<List<TaxonomyClientResponse>> response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/internal/v1/taxonomies/batch")
						.queryParam("ids", idsParam)
						.build())
				.retrieve()
				.body(TAXONOMY_BATCH_TYPE);
		List<TaxonomyClientResponse> data = ApiClientSupport.requireData(response);
		return data != null ? data : List.of();
	}

	public SocialPlatformClientResponse getSocialPlatform(UUID platformId) {
		ApiResponse<SocialPlatformClientResponse> response = restClient.get()
				.uri("/api/v1/social-platforms/{id}", platformId)
				.retrieve()
				.body(SOCIAL_PLATFORM_TYPE);
		return ApiClientSupport.requireData(response);
	}

	public EntityTaxonomyClientResponse assignTaxonomy(UUID entityId, UUID taxonomyId) {
		EntityTaxonomyAssignClientRequest body = new EntityTaxonomyAssignClientRequest(
				entityId,
				ENTITY_TYPE_PERSON,
				taxonomyId);
		ApiResponse<EntityTaxonomyClientResponse> response = restClient.post()
				.uri("/internal/v1/entity-taxonomies")
				.body(body)
				.retrieve()
				.body(ENTITY_TAXONOMY_TYPE);
		return ApiClientSupport.requireData(response);
	}

	public void removeTaxonomy(UUID entityId, UUID taxonomyId) {
		restClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path("/internal/v1/entity-taxonomies")
						.queryParam("entityId", entityId)
						.queryParam("entityType", ENTITY_TYPE_PERSON)
						.queryParam("taxonomyId", taxonomyId)
						.build())
				.retrieve()
				.toBodilessEntity();
	}

}
