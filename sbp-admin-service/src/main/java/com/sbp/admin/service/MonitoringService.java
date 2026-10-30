package com.sbp.admin.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import com.sbp.admin.config.MonitoringProperties;
import com.sbp.admin.dto.MonitoringDtos.EventsResponse;
import com.sbp.admin.dto.MonitoringDtos.InstanceDetailResponse;
import com.sbp.admin.dto.MonitoringDtos.InstanceHealth;
import com.sbp.admin.dto.MonitoringDtos.InstanceSummary;
import com.sbp.admin.dto.MonitoringDtos.InstancesResponse;
import com.sbp.admin.dto.MonitoringDtos.MetricsOverviewResponse;
import com.sbp.admin.dto.MonitoringDtos.OverviewResponse;
import com.sbp.admin.dto.MonitoringDtos.ServiceBrief;
import com.sbp.admin.dto.MonitoringDtos.ServiceDetailResponse;
import com.sbp.admin.dto.MonitoringDtos.ServiceHealthResponse;
import com.sbp.admin.dto.MonitoringDtos.ServiceSummary;
import com.sbp.admin.dto.MonitoringDtos.ServicesResponse;
import com.sbp.admin.dto.MonitoringDtos.Summary;
import com.sbp.admin.dto.MonitoringStatus;

@Service
public class MonitoringService {

	private final DiscoveryClient discoveryClient;

	private final ActuatorProbeService actuatorProbeService;

	private final PrometheusQueryService prometheusQueryService;

	private final InfrastructureHealthService infrastructureHealthService;

	private final MonitoringEventTracker eventTracker;

	private final MonitoringStatusCalculator statusCalculator;

	private final MonitoringProperties properties;

	public MonitoringService(DiscoveryClient discoveryClient, ActuatorProbeService actuatorProbeService,
			PrometheusQueryService prometheusQueryService, InfrastructureHealthService infrastructureHealthService,
			MonitoringEventTracker eventTracker, MonitoringStatusCalculator statusCalculator,
			MonitoringProperties properties) {
		this.discoveryClient = discoveryClient;
		this.actuatorProbeService = actuatorProbeService;
		this.prometheusQueryService = prometheusQueryService;
		this.infrastructureHealthService = infrastructureHealthService;
		this.eventTracker = eventTracker;
		this.statusCalculator = statusCalculator;
		this.properties = properties;
	}

	@Cacheable("monitoringSnapshot")
	public RegistrySnapshot snapshot() {
		boolean registryAvailable = isRegistryAvailable();
		List<ServiceSummary> services = registryAvailable ? buildServiceSummaries() : List.of();
		List<InstanceSummary> flatInstances = services.stream().flatMap(s -> s.instances().stream()).toList();
		eventTracker.recordRegistrySnapshot(flatInstances);
		return new RegistrySnapshot(registryAvailable, services, flatInstances);
	}

	public OverviewResponse overview() {
		RegistrySnapshot snap = snapshot();
		boolean promAvailable = prometheusQueryService.isAvailable();
		List<MonitoringStatus> serviceStatuses = snap.services().stream().map(ServiceSummary::status).toList();
		MonitoringStatus systemStatus = statusCalculator.aggregateSystemStatus(serviceStatuses, snap.registryAvailable(),
				promAvailable);
		Summary summary = toSummary(snap);
		List<ServiceBrief> briefs = snap.services().stream()
				.map(s -> new ServiceBrief(s.name(), s.status(), s.instanceCount()))
				.sorted(Comparator.comparing(ServiceBrief::name))
				.toList();
		String notes = !snap.registryAvailable() ? "Eureka registry unavailable; service data may be stale."
				: !promAvailable ? "Prometheus unavailable; metrics endpoints may be incomplete." : null;
		return new OverviewResponse(properties.getEnvironmentName(), Instant.now(), systemStatus, summary, briefs,
				snap.registryAvailable(), promAvailable, notes);
	}

	public ServicesResponse listServices() {
		RegistrySnapshot snap = snapshot();
		return new ServicesResponse(Instant.now(), snap.registryAvailable(), snap.services());
	}

	public Optional<ServiceDetailResponse> serviceDetail(String serviceName) {
		return findService(serviceName).map(service -> {
			List<String> infraDeps = properties.getInfrastructure()
					.stream()
					.filter(i -> i.getUsedByServices().stream().anyMatch(s -> s.equalsIgnoreCase(service.name())))
					.map(i -> i.getName())
					.toList();
			Map<String, String> metadata = service.instances().isEmpty() ? Map.of()
					: new HashMap<>(service.instances().get(0).metadata());
			return new ServiceDetailResponse(service.name(), properties.getEnvironmentName(), service.status(),
					service.instances(), infraDeps, metadata);
		});
	}

	public Optional<ServiceHealthResponse> serviceHealth(String serviceName) {
		return findService(serviceName).map(service -> {
			List<InstanceHealth> health = service.instances().stream().map(this::probeInstance).toList();
			List<MonitoringStatus> statuses = health.stream().map(InstanceHealth::status).toList();
			MonitoringStatus overall = statusCalculator.aggregateInstanceStatuses(statuses);
			return new ServiceHealthResponse(service.name(), overall, Instant.now(), health);
		});
	}

	public InstancesResponse listInstances(String service, String status, String environment) {
		RegistrySnapshot snap = snapshot();
		List<InstanceSummary> instances = snap.instances().stream()
				.filter(i -> service == null || i.serviceName().equalsIgnoreCase(service))
				.filter(i -> status == null || i.status().name().equalsIgnoreCase(status))
				.filter(i -> environment == null || environment.equalsIgnoreCase(i.environment()))
				.toList();
		return new InstancesResponse(Instant.now(), instances);
	}

	public Optional<InstanceDetailResponse> instanceDetail(String instanceId) {
		RegistrySnapshot snap = snapshot();
		return snap.instances().stream().filter(i -> i.instanceId().equals(instanceId)).findFirst().map(instance -> {
			InstanceHealth health = probeInstance(instance);
			return new InstanceDetailResponse(instance, health);
		});
	}

	public MetricsOverviewResponse metricsOverview() {
		return prometheusQueryService.queryOverview();
	}

	public EventsResponse events(String service, String status, String environment, int page, int size) {
		int safeSize = Math.min(Math.max(size, 1), 100);
		int safePage = Math.max(page, 0);
		long total = eventTracker.countEvents(service, status, environment);
		return new EventsResponse(safePage, safeSize, total,
				eventTracker.listEvents(service, status, environment, safePage, safeSize));
	}

	public InfrastructureHealthService infrastructureHealthService() {
		return infrastructureHealthService;
	}

	private Optional<ServiceSummary> findService(String serviceName) {
		if (serviceName == null) {
			return Optional.empty();
		}
		return snapshot().services()
				.stream()
				.filter(s -> s.name().equalsIgnoreCase(serviceName))
				.findFirst();
	}

	private InstanceHealth probeInstance(InstanceSummary instance) {
		return actuatorProbeService.probeHealth(instance.instanceId(), instance.host(), instance.managementPort());
	}

	private List<ServiceSummary> buildServiceSummaries() {
		List<String> services = discoveryClient.getServices();
		List<ServiceSummary> summaries = new ArrayList<>();
		for (String serviceId : services) {
			if ("sbp-discovery-server".equalsIgnoreCase(serviceId)) {
				continue;
			}
			List<InstanceSummary> instances = discoveryClient.getInstances(serviceId)
					.stream()
					.map(this::toInstanceSummary)
					.toList();
			List<MonitoringStatus> statuses = instances.stream().map(InstanceSummary::status).toList();
			MonitoringStatus status = statusCalculator.aggregateInstanceStatuses(statuses);
			summaries.add(new ServiceSummary(serviceId.toUpperCase(Locale.ROOT), status, instances.size(), instances));
		}
		summaries.sort(Comparator.comparing(ServiceSummary::name));
		return summaries;
	}

	private InstanceSummary toInstanceSummary(ServiceInstance instance) {
		int managementPort = resolveManagementPort(instance);
		String host = instance.getHost();
		int port = instance.getPort();
		String instanceId = instance.getInstanceId() != null ? instance.getInstanceId()
				: instance.getServiceId() + ":" + host + ":" + port;
		Map<String, String> metadata = instance.getMetadata() != null ? instance.getMetadata() : Map.of();
		String environment = metadata.getOrDefault("environment", properties.getEnvironmentName());
		String managementUrl = "http://" + host + ":" + managementPort + "/actuator";
		MonitoringStatus status = actuatorProbeService.probeHealth(instanceId, host, managementPort).status();
		return new InstanceSummary(instanceId, instance.getServiceId().toUpperCase(Locale.ROOT), host, port,
				managementPort, managementUrl, status, environment, metadata);
	}

	private int resolveManagementPort(ServiceInstance instance) {
		Map<String, String> metadata = instance.getMetadata();
		if (metadata != null && metadata.containsKey("management.port")) {
			try {
				return Integer.parseInt(metadata.get("management.port"));
			}
			catch (NumberFormatException ignored) {
				// fall through
			}
		}
		return instance.getPort() + 1000;
	}

	private Summary toSummary(RegistrySnapshot snap) {
		int totalServices = snap.services().size();
		int healthyServices = (int) snap.services().stream().filter(s -> s.status() == MonitoringStatus.UP).count();
		int degradedServices = (int) snap.services().stream().filter(s -> s.status() == MonitoringStatus.DEGRADED).count();
		int unhealthyServices = (int) snap.services().stream().filter(s -> s.status() == MonitoringStatus.DOWN).count();
		int unknownServices = (int) snap.services().stream().filter(s -> s.status() == MonitoringStatus.UNKNOWN).count();
		int totalInstances = snap.instances().size();
		int healthyInstances = (int) snap.instances().stream().filter(i -> i.status() == MonitoringStatus.UP).count();
		int unhealthyInstances = (int) snap.instances().stream().filter(i -> i.status() == MonitoringStatus.DOWN).count();
		int unknownInstances = (int) snap.instances().stream().filter(i -> i.status() == MonitoringStatus.UNKNOWN).count();
		return new Summary(totalServices, healthyServices, degradedServices, unhealthyServices, unknownServices,
				totalInstances, healthyInstances, unhealthyInstances, unknownInstances,
				properties.getInfrastructure().size());
	}

	private boolean isRegistryAvailable() {
		try {
			discoveryClient.getServices();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	public record RegistrySnapshot(boolean registryAvailable, List<ServiceSummary> services,
			List<InstanceSummary> instances) {
	}

}
