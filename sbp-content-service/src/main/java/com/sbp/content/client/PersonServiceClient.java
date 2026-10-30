package com.sbp.content.client;

import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sbp.common.client.ApiClientSupport;
import com.sbp.common.response.ApiResponse;
import com.sbp.content.client.dto.PersonSummaryClientResponse;
import com.sbp.content.config.ClientProperties;

@Component
public class PersonServiceClient {

	private static final ParameterizedTypeReference<ApiResponse<PersonSummaryClientResponse>> PERSON_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;

	public PersonServiceClient(RestClient.Builder restClientBuilder, ClientProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.getPersonBaseUrl()).build();
	}

	public PersonSummaryClientResponse getPerson(UUID personId) {
		ApiResponse<PersonSummaryClientResponse> response = restClient.get()
				.uri("/api/v1/persons/{personId}", personId)
				.retrieve()
				.body(PERSON_TYPE);
		return ApiClientSupport.requireData(response);
	}

}
