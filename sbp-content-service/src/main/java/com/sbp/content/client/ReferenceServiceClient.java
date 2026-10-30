package com.sbp.content.client;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sbp.common.client.ApiClientSupport;
import com.sbp.common.response.ApiResponse;
import com.sbp.content.client.dto.TaxonomyClientResponse;
import com.sbp.content.config.ClientProperties;

@Component
public class ReferenceServiceClient {

	private static final ParameterizedTypeReference<ApiResponse<List<TaxonomyClientResponse>>> TAXONOMY_BATCH_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;

	public ReferenceServiceClient(RestClient.Builder restClientBuilder, ClientProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getReferenceBaseUrl()).build();
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

}
