package com.sbp.admin.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sbp.admin.dto.MonitoringDtos.InstanceSummary;
import com.sbp.admin.dto.MonitoringDtos.MonitoringEvent;
import com.sbp.admin.dto.MonitoringStatus;

@Component
public class MonitoringEventTracker {

	private static final int MAX_EVENTS = 500;

	private final CopyOnWriteArrayList<MonitoringEvent> events = new CopyOnWriteArrayList<>();

	private final Map<String, MonitoringStatus> lastInstanceStatus = new ConcurrentHashMap<>();

	private final Map<String, Integer> lastServiceInstanceCount = new ConcurrentHashMap<>();

	public void recordRegistrySnapshot(List<InstanceSummary> instances) {
		Map<String, Long> counts = new ConcurrentHashMap<>();
		for (InstanceSummary instance : instances) {
			counts.merge(instance.serviceName(), 1L, Long::sum);
			MonitoringStatus previous = lastInstanceStatus.put(instance.instanceId(), instance.status());
			if (previous == null) {
				addEvent("INSTANCE_REGISTERED", instance.serviceName(), instance.instanceId(), null, instance.status(),
						"Instance registered");
			}
			else if (previous != instance.status()) {
				addEvent("HEALTH_STATUS_CHANGED", instance.serviceName(), instance.instanceId(), previous,
						instance.status(), "Instance health changed");
			}
		}
		counts.forEach((service, count) -> {
			Integer previousCount = lastServiceInstanceCount.put(service, count.intValue());
			if (previousCount != null && !previousCount.equals(count.intValue())) {
				addEvent("SERVICE_AVAILABILITY_CHANGED", service, null, null, MonitoringStatus.UNKNOWN,
						"Instance count changed from " + previousCount + " to " + count);
			}
		});
	}

	public List<MonitoringEvent> listEvents(String service, String status, String environment, int page, int size) {
		List<MonitoringEvent> filtered = events.stream()
				.filter(e -> service == null || service.equalsIgnoreCase(e.serviceName()))
				.filter(e -> status == null || (e.currentStatus() != null && e.currentStatus().name().equalsIgnoreCase(status)))
				.filter(e -> environment == null || (e.message() != null && e.message().toLowerCase().contains(environment.toLowerCase())))
				.toList();
		int from = Math.max(0, page * size);
		int to = Math.min(filtered.size(), from + size);
		if (from >= filtered.size()) {
			return Collections.emptyList();
		}
		return new ArrayList<>(filtered.subList(from, to));
	}

	public long countEvents(String service, String status, String environment) {
		return events.stream()
				.filter(e -> service == null || service.equalsIgnoreCase(e.serviceName()))
				.filter(e -> status == null || (e.currentStatus() != null && e.currentStatus().name().equalsIgnoreCase(status)))
				.filter(e -> environment == null || (e.message() != null && e.message().toLowerCase().contains(environment.toLowerCase())))
				.count();
	}

	@Scheduled(fixedDelayString = "${sbp.monitoring.registry-poll-ms:20000}")
	public void noopScheduledHook() {
		// Snapshot comparison is invoked by MonitoringService after each refresh.
	}

	private void addEvent(String type, String serviceName, String instanceId, MonitoringStatus previous,
			MonitoringStatus current, String message) {
		events.add(0, new MonitoringEvent(Instant.now(), type, serviceName, instanceId, previous, current, message));
		while (events.size() > MAX_EVENTS) {
			events.remove(events.size() - 1);
		}
	}

}
