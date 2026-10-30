package com.sbp.content.client;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sbp.common.client.ApiClientSupport;
import com.sbp.common.response.ApiResponse;
import com.sbp.content.client.dto.StudioClientResponse;
import com.sbp.content.config.ClientProperties;

@Component
public class StudioServiceClient {

	private static final ParameterizedTypeReference<ApiResponse<StudioClientResponse>> STUDIO_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;

	public StudioServiceClient(RestClient.Builder restClientBuilder, ClientProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getStudioBaseUrl()).build();
	}

	public StudioClientResponse getStudio(UUID studioId) {
		ApiResponse<StudioClientResponse> response = restClient.get()
				.uri("/api/v1/studios/internal/{studioId}", studioId)
				.retrieve()
				.body(STUDIO_TYPE);
		return ApiClientSupport.requireData(response);
	}

}
