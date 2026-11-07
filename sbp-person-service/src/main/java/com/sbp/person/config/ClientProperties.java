package com.sbp.person.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "sbp.clients")
@Getter
@Setter
public class ClientProperties {

	private String referenceBaseUrl = "http://localhost:8087";

	private String studioBaseUrl = "http://localhost:8085";

	private String fileStorageBaseUrl = "http://localhost:8088";

}
