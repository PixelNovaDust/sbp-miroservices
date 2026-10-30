package com.sbp.person.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

}
