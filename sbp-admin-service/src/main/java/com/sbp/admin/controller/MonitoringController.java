package com.sbp.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sbp.admin.dto.MonitoringDtos.EventsResponse;
import com.sbp.admin.dto.MonitoringDtos.InstanceDetailResponse;
import com.sbp.admin.dto.MonitoringDtos.InstancesResponse;
import com.sbp.admin.dto.MonitoringDtos.MetricsOverviewResponse;
import com.sbp.admin.dto.MonitoringDtos.OverviewResponse;
import com.sbp.admin.dto.MonitoringDtos.ServiceDetailResponse;
import com.sbp.admin.dto.MonitoringDtos.ServiceHealthResponse;
import com.sbp.admin.dto.MonitoringDtos.ServicesResponse;
import com.sbp.admin.service.MonitoringService;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

	private final MonitoringService monitoringService;

	public MonitoringController(MonitoringService monitoringService) {
		this.monitoringService = monitoringService;
	}

	@GetMapping("/overview")
	public OverviewResponse overview() {
		return monitoringService.overview();
	}

	@GetMapping("/services")
	public ServicesResponse services() {
		return monitoringService.listServices();
	}

	@GetMapping("/services/{serviceName}")
	public ServiceDetailResponse serviceDetail(@PathVariable String serviceName) {
		return monitoringService.serviceDetail(serviceName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not registered"));
	}

	@GetMapping("/services/{serviceName}/health")
	public ServiceHealthResponse serviceHealth(@PathVariable String serviceName) {
		return monitoringService.serviceHealth(serviceName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not registered"));
	}

	@GetMapping("/instances")
	public InstancesResponse instances(@RequestParam(required = false) String service,
			@RequestParam(required = false) String status, @RequestParam(required = false) String environment) {
		return monitoringService.listInstances(service, status, environment);
	}

	@GetMapping("/instances/{instanceId}")
	public InstanceDetailResponse instanceDetail(@PathVariable String instanceId) {
		return monitoringService.instanceDetail(instanceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instance not found"));
	}

	@GetMapping("/infrastructure")
	public Object infrastructure() {
		return monitoringService.infrastructureHealthService().listComponents();
	}

	@GetMapping("/infrastructure/{name}/health")
	public Object infrastructureHealth(@PathVariable String name) {
		return monitoringService.infrastructureHealthService()
				.componentHealth(name)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Infrastructure component not found"));
	}

	@GetMapping("/metrics")
	public MetricsOverviewResponse metrics() {
		return monitoringService.metricsOverview();
	}

	@GetMapping("/events")
	public EventsResponse events(@RequestParam(required = false) String service,
			@RequestParam(required = false) String status, @RequestParam(required = false) String environment,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return monitoringService.events(service, status, environment, page, size);
	}

}
