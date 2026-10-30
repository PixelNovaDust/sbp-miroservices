package com.sbp.admin.service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.sbp.admin.config.MonitoringProperties;
import com.sbp.admin.dto.MonitoringDtos.InstanceHealth;
import com.sbp.admin.dto.MonitoringStatus;
import com.sbp.admin.util.TargetHostValidator;

@Service
public class ActuatorProbeService {

	private static final Logger log = LoggerFactory.getLogger(ActuatorProbeService.class);

	private final RestClient restClient;

	private final MonitoringProperties properties;

	private final MonitoringStatusCalculator statusCalculator;

	private final TargetHostValidator hostValidator;

	public ActuatorProbeService(RestClient.Builder restClientBuilder, MonitoringProperties properties,
			MonitoringStatusCalculator statusCalculator, TargetHostValidator hostValidator) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(properties.getHealthConnectTimeoutMs()));
		requestFactory.setReadTimeout(Duration.ofMillis(properties.getHealthReadTimeoutMs()));
		this.restClient = restClientBuilder.requestFactory(requestFactory).build();
		this.properties = properties;
		this.statusCalculator = statusCalculator;
		this.hostValidator = hostValidator;
	}

	public InstanceHealth probeHealth(String instanceId, String host, int managementPort) {
		if (!hostValidator.isTrusted(host)) {
			return new InstanceHealth(instanceId, MonitoringStatus.UNKNOWN, Instant.now(), 0, Collections.emptyMap(),
					"Health probe blocked for untrusted host");
		}
		URI uri = URI.create("http://" + host + ":" + managementPort + "/actuator/health");
		long start = System.nanoTime();
		try {
			Map<String, Object> body = restClient.get()
					.uri(uri)
					.retrieve()
					.body(new ParameterizedTypeReference<Map<String, Object>>() {
					});
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			String status = body != null && body.get("status") != null ? body.get("status").toString() : null;
			@SuppressWarnings("unchecked")
			Map<String, Object> components = body != null && body.get("components") instanceof Map<?, ?> map
					? (Map<String, Object>) map : Collections.emptyMap();
			return new InstanceHealth(instanceId, statusCalculator.mapActuatorStatus(status), Instant.now(), elapsedMs,
					components, null);
		}
		catch (RestClientException ex) {
			log.debug("Health probe failed for {}: {}", uri, ex.getMessage());
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			return new InstanceHealth(instanceId, MonitoringStatus.DOWN, Instant.now(), elapsedMs,
					Collections.emptyMap(), "Health probe failed");
		}
	}

}
