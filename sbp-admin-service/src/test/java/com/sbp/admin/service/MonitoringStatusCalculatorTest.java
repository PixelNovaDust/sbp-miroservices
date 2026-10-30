package com.sbp.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sbp.admin.dto.MonitoringStatus;

class MonitoringStatusCalculatorTest {

	private final MonitoringStatusCalculator calculator = new MonitoringStatusCalculator();

	@Test
	void aggregateInstanceStatusesReturnsDegradedWhenMixed() {
		assertThat(calculator.aggregateInstanceStatuses(List.of(MonitoringStatus.UP, MonitoringStatus.DOWN)))
				.isEqualTo(MonitoringStatus.DEGRADED);
	}

	@Test
	void aggregateInstanceStatusesReturnsUpWhenAllUp() {
		assertThat(calculator.aggregateInstanceStatuses(List.of(MonitoringStatus.UP, MonitoringStatus.UP)))
				.isEqualTo(MonitoringStatus.UP);
	}

	@Test
	void mapActuatorStatusMapsKnownValues() {
		assertThat(calculator.mapActuatorStatus("UP")).isEqualTo(MonitoringStatus.UP);
		assertThat(calculator.mapActuatorStatus("DOWN")).isEqualTo(MonitoringStatus.DOWN);
	}

}
