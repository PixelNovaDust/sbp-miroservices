package com.sbp.reference.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.sbp.common.client.FileStorageServiceClient;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	FileStorageServiceClient fileStorageServiceClient(RestClient.Builder restClientBuilder, ClientProperties properties) {
		return new FileStorageServiceClient(properties.getFileStorageBaseUrl(), restClientBuilder);
	}

}
