package com.sbp.admin.service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sbp.admin.config.MonitoringProperties;
import com.sbp.admin.config.MonitoringProperties.InfrastructureComponentProperties;
import com.sbp.admin.dto.MonitoringDtos.InfrastructureComponent;
import com.sbp.admin.dto.MonitoringDtos.InfrastructureHealthResponse;
import com.sbp.admin.dto.MonitoringDtos.InfrastructureResponse;
import com.sbp.admin.dto.MonitoringStatus;
import com.sbp.admin.util.TargetHostValidator;

@Service
public class InfrastructureHealthService {

	private final MonitoringProperties properties;

	private final TargetHostValidator hostValidator;

	public InfrastructureHealthService(MonitoringProperties properties, TargetHostValidator hostValidator) {
		this.properties = properties;
		this.hostValidator = hostValidator;
	}

	public InfrastructureResponse listComponents() {
		List<InfrastructureComponent> components = new ArrayList<>();
		for (InfrastructureComponentProperties cfg : properties.getInfrastructure()) {
			components.add(checkComponent(cfg));
		}
		return new InfrastructureResponse(Instant.now(), components);
	}

	public Optional<InfrastructureHealthResponse> componentHealth(String name) {
		return properties.getInfrastructure()
				.stream()
				.filter(c -> c.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(this::toHealthResponse);
	}

	private InfrastructureComponent checkComponent(InfrastructureComponentProperties cfg) {
		ProbeResult probe = probeTcp(cfg.getHost(), cfg.getPort());
		return new InfrastructureComponent(cfg.getName(), cfg.getType(), cfg.getEnvironment(), cfg.getHost(),
				cfg.getPort(), probe.status(), cfg.getUsedByServices(), probe.checkedAt(), probe.responseTimeMs(),
				probe.details());
	}

	private InfrastructureHealthResponse toHealthResponse(InfrastructureComponentProperties cfg) {
		ProbeResult probe = probeTcp(cfg.getHost(), cfg.getPort());
		return new InfrastructureHealthResponse(cfg.getName(), probe.status(), probe.checkedAt(), probe.responseTimeMs(),
				probe.status() == MonitoringStatus.UP ? "REACHABLE" : "UNREACHABLE",
				java.util.Map.of("type", cfg.getType(), "host", cfg.getHost(), "port", cfg.getPort()));
	}

	private ProbeResult probeTcp(String host, int port) {
		if (!hostValidator.isTrusted(host)) {
			return new ProbeResult(MonitoringStatus.UNKNOWN, Instant.now(), null, "Host not in trusted allowlist");
		}
		long start = System.nanoTime();
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), 2000);
			long elapsed = (System.nanoTime() - start) / 1_000_000;
			return new ProbeResult(MonitoringStatus.UP, Instant.now(), elapsed, "TCP connect succeeded");
		}
		catch (Exception ex) {
			long elapsed = (System.nanoTime() - start) / 1_000_000;
			return new ProbeResult(MonitoringStatus.DOWN, Instant.now(), elapsed, "TCP connect failed");
		}
	}

	private record ProbeResult(MonitoringStatus status, Instant checkedAt, Long responseTimeMs, String details) {
	}

}
