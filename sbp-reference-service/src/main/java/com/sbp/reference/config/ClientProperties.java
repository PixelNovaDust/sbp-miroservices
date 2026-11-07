package com.sbp.reference.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "sbp.clients")
@Getter
@Setter
public class ClientProperties {

	private String fileStorageBaseUrl = "http://localhost:8088";

}
