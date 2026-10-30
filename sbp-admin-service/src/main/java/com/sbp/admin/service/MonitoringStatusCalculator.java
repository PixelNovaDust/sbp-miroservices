package com.sbp.admin.service;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.sbp.admin.dto.MonitoringStatus;

@Component
public class MonitoringStatusCalculator {

	public MonitoringStatus aggregateInstanceStatuses(Collection<MonitoringStatus> instanceStatuses) {
		if (instanceStatuses == null || instanceStatuses.isEmpty()) {
			return MonitoringStatus.NOT_REGISTERED;
		}
		long up = instanceStatuses.stream().filter(s -> s == MonitoringStatus.UP).count();
		long down = instanceStatuses.stream().filter(s -> s == MonitoringStatus.DOWN).count();
		long unknown = instanceStatuses.stream().filter(s -> s == MonitoringStatus.UNKNOWN).count();
		int total = instanceStatuses.size();
		if (down == total) {
			return MonitoringStatus.DOWN;
		}
		if (up == total) {
			return MonitoringStatus.UP;
		}
		if (up > 0 && (down > 0 || unknown > 0)) {
			return MonitoringStatus.DEGRADED;
		}
		if (unknown == total) {
			return MonitoringStatus.UNKNOWN;
		}
		return MonitoringStatus.DEGRADED;
	}

	public MonitoringStatus mapActuatorStatus(String status) {
		if (status == null) {
			return MonitoringStatus.UNKNOWN;
		}
		return switch (status.toUpperCase()) {
			case "UP" -> MonitoringStatus.UP;
			case "DOWN", "OUT_OF_SERVICE" -> MonitoringStatus.DOWN;
			default -> MonitoringStatus.UNKNOWN;
		};
	}

	public MonitoringStatus aggregateSystemStatus(Collection<MonitoringStatus> serviceStatuses, boolean registryAvailable,
			boolean prometheusAvailable) {
		if (!registryAvailable) {
			return MonitoringStatus.UNKNOWN;
		}
		if (serviceStatuses == null || serviceStatuses.isEmpty()) {
			return MonitoringStatus.UNKNOWN;
		}
		long down = serviceStatuses.stream().filter(s -> s == MonitoringStatus.DOWN).count();
		long degraded = serviceStatuses.stream().filter(s -> s == MonitoringStatus.DEGRADED).count();
		long unknown = serviceStatuses.stream().filter(s -> s == MonitoringStatus.UNKNOWN).count();
		if (down > 0 && down == serviceStatuses.size()) {
			return MonitoringStatus.DOWN;
		}
		if (degraded > 0 || (down > 0 && down < serviceStatuses.size())) {
			return MonitoringStatus.DEGRADED;
		}
		if (unknown == serviceStatuses.size() && !prometheusAvailable) {
			return MonitoringStatus.UNKNOWN;
		}
		return MonitoringStatus.UP;
	}

}
