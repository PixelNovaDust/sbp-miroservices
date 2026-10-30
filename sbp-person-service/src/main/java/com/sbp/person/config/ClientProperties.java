package com.sbp.person.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbp.clients")
public class ClientProperties {

	private String referenceBaseUrl = "http://localhost:8087";

	private String studioBaseUrl = "http://localhost:8085";

	public String getReferenceBaseUrl() {
		return referenceBaseUrl;
	}

	public void setReferenceBaseUrl(String referenceBaseUrl) {
		this.referenceBaseUrl = referenceBaseUrl;
	}

	public String getStudioBaseUrl() {
		return studioBaseUrl;
	}

	public void setStudioBaseUrl(String studioBaseUrl) {
		this.studioBaseUrl = studioBaseUrl;
	}

}
