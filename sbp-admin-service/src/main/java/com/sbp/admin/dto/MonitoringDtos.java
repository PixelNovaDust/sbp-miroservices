package com.sbp.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class MonitoringDtos {

	private MonitoringDtos() {
	}

	public record OverviewResponse(
			String environment,
			Instant timestamp,
			MonitoringStatus status,
			Summary summary,
			List<ServiceBrief> services,
			boolean registryAvailable,
			boolean prometheusAvailable,
			String notes) {
	}

	public record Summary(
			int totalServices,
			int healthyServices,
			int degradedServices,
			int unhealthyServices,
			int unknownServices,
			int totalInstances,
			int healthyInstances,
			int unhealthyInstances,
			int unknownInstances,
			int infrastructureComponents) {
	}

	public record ServiceBrief(String name, MonitoringStatus status, int instances) {
	}

	public record ServicesResponse(
			Instant timestamp,
			boolean registryAvailable,
			List<ServiceSummary> services) {
	}

	public record ServiceSummary(
			String name,
			MonitoringStatus status,
			int instanceCount,
			List<InstanceSummary> instances) {
	}

	public record InstanceSummary(
			String instanceId,
			String serviceName,
			String host,
			int port,
			int managementPort,
			String managementUrl,
			MonitoringStatus status,
			String environment,
			Map<String, String> metadata) {
	}

	public record ServiceDetailResponse(
			String name,
			String environment,
			MonitoringStatus status,
			List<InstanceSummary> instances,
			List<String> infrastructureDependencies,
			Map<String, String> metadata) {
	}

	public record ServiceHealthResponse(
			String serviceName,
			MonitoringStatus overallStatus,
			Instant timestamp,
			List<InstanceHealth> instances) {
	}

	public record InstanceHealth(
			String instanceId,
			MonitoringStatus status,
			Instant checkedAt,
			long responseTimeMs,
			Map<String, Object> components,
			String error) {
	}

	public record InstancesResponse(Instant timestamp, List<InstanceSummary> instances) {
	}

	public record InstanceDetailResponse(InstanceSummary instance, InstanceHealth health) {
	}

	public record InfrastructureResponse(Instant timestamp, List<InfrastructureComponent> components) {
	}

	public record InfrastructureComponent(
			String name,
			String type,
			String environment,
			String host,
			int port,
			MonitoringStatus status,
			List<String> usedByServices,
			Instant lastCheckedAt,
			Long responseTimeMs,
			String details) {
	}

	public record InfrastructureHealthResponse(
			String name,
			MonitoringStatus status,
			Instant checkedAt,
			Long responseTimeMs,
			String connectivity,
			Map<String, Object> details) {
	}

	public record MetricsOverviewResponse(
			Instant timestamp,
			boolean prometheusAvailable,
			Map<String, Object> metrics,
			String error) {
	}

	public record MonitoringEvent(
			Instant timestamp,
			String type,
			String serviceName,
			String instanceId,
			MonitoringStatus previousStatus,
			MonitoringStatus currentStatus,
			String message) {
	}

	public record EventsResponse(
			int page,
			int size,
			long total,
			List<MonitoringEvent> events) {
	}

	public record ApiError(Instant timestamp, int status, String error, String message, String path) {
	}

}
