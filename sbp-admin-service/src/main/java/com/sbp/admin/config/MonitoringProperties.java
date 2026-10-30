package com.sbp.admin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbp.monitoring")
public class MonitoringProperties {

	private String environmentName = "dev";

	private String prometheusUrl = "http://localhost:9090";

	private int healthConnectTimeoutMs = 2000;

	private int healthReadTimeoutMs = 3000;

	private int cacheTtlSeconds = 15;

	private List<String> trustedHostSuffixes = new ArrayList<>();

	private List<InfrastructureComponentProperties> infrastructure = new ArrayList<>();

	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getPrometheusUrl() {
		return prometheusUrl;
	}

	public void setPrometheusUrl(String prometheusUrl) {
		this.prometheusUrl = prometheusUrl;
	}

	public int getHealthConnectTimeoutMs() {
		return healthConnectTimeoutMs;
	}

	public void setHealthConnectTimeoutMs(int healthConnectTimeoutMs) {
		this.healthConnectTimeoutMs = healthConnectTimeoutMs;
	}

	public int getHealthReadTimeoutMs() {
		return healthReadTimeoutMs;
	}

	public void setHealthReadTimeoutMs(int healthReadTimeoutMs) {
		this.healthReadTimeoutMs = healthReadTimeoutMs;
	}

	public int getCacheTtlSeconds() {
		return cacheTtlSeconds;
	}

	public void setCacheTtlSeconds(int cacheTtlSeconds) {
		this.cacheTtlSeconds = cacheTtlSeconds;
	}

	public List<String> getTrustedHostSuffixes() {
		return trustedHostSuffixes;
	}

	public void setTrustedHostSuffixes(List<String> trustedHostSuffixes) {
		this.trustedHostSuffixes = trustedHostSuffixes;
	}

	public List<InfrastructureComponentProperties> getInfrastructure() {
		return infrastructure;
	}

	public void setInfrastructure(List<InfrastructureComponentProperties> infrastructure) {
		this.infrastructure = infrastructure;
	}

	public static class InfrastructureComponentProperties {

		private String name;

		private String type;

		private String environment;

		private String host;

		private int port;

		private List<String> usedByServices = new ArrayList<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getEnvironment() {
			return environment;
		}

		public void setEnvironment(String environment) {
			this.environment = environment;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public List<String> getUsedByServices() {
			return usedByServices;
		}

		public void setUsedByServices(List<String> usedByServices) {
			this.usedByServices = usedByServices;
		}

	}

}
