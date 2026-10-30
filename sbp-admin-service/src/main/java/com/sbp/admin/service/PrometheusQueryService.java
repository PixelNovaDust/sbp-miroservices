package com.sbp.admin.service;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.sbp.admin.config.MonitoringProperties;
import com.sbp.admin.dto.MonitoringDtos.MetricsOverviewResponse;

@Service
public class PrometheusQueryService {

	private final RestClient restClient;

	private final MonitoringProperties properties;

	public PrometheusQueryService(RestClient.Builder restClientBuilder, MonitoringProperties properties) {
		this.restClient = restClientBuilder.build();
		this.properties = properties;
	}

	public MetricsOverviewResponse queryOverview() {
		try {
			Map<String, Object> metrics = new HashMap<>();
			metrics.put("up", queryScalar("count(up==1)"));
			metrics.put("httpServerRequestsRate", queryScalar("sum(rate(http_server_requests_seconds_count[5m]))"));
			metrics.put("jvmMemoryUsed", queryScalar("sum(jvm_memory_used_bytes)"));
			metrics.put("jvmThreadsLive", queryScalar("sum(jvm_threads_live_threads)"));
			metrics.put("processCpuUsage", queryScalar("avg(process_cpu_usage)"));
			return new MetricsOverviewResponse(Instant.now(), true, metrics, null);
		}
		catch (RestClientException ex) {
			return new MetricsOverviewResponse(Instant.now(), false, Map.of(), "Prometheus unavailable");
		}
	}

	public boolean isAvailable() {
		try {
			restClient.get().uri(URI.create(properties.getPrometheusUrl() + "/-/ready")).retrieve().toBodilessEntity();
			return true;
		}
		catch (RestClientException ex) {
			return false;
		}
	}

	private Object queryScalar(String promql) {
		URI uri = URI.create(properties.getPrometheusUrl() + "/api/v1/query?query=" + promql.replace(" ", "%20"));
		Map<String, Object> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {
				});
		if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
			return null;
		}
		Object result = data.get("result");
		if (!(result instanceof List<?> list) || list.isEmpty()) {
			return null;
		}
		Object first = list.get(0);
		if (first instanceof Map<?, ?> row && row.get("value") instanceof List<?> valuePair && valuePair.size() > 1) {
			return valuePair.get(1);
		}
		return null;
	}

}
