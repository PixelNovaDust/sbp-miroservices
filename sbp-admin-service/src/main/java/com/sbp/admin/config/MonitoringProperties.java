package com.sbp.admin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "sbp.monitoring")
@Getter
@Setter
public class MonitoringProperties {

	private String environmentName = "dev";

	private String prometheusUrl = "http://localhost:9090";

	private int healthConnectTimeoutMs = 2000;

	private int healthReadTimeoutMs = 3000;

	private int cacheTtlSeconds = 15;

	private List<String> trustedHostSuffixes = new ArrayList<>();

	private List<InfrastructureComponentProperties> infrastructure = new ArrayList<>();

	@Getter
	@Setter
	public static class InfrastructureComponentProperties {

		private String name;

		private String type;

		private String environment;

		private String host;

		private int port;

		private List<String> usedByServices = new ArrayList<>();

	}

}
