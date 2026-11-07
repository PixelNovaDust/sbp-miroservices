package com.sbp.admin.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.sbp.admin.config.MonitoringProperties;

@Component
@RequiredArgsConstructor

public class TargetHostValidator {

	private final MonitoringProperties properties;


	public boolean isTrusted(String host) {
		if (host == null || host.isBlank()) {
			return false;
		}
		String normalized = host.toLowerCase();
		for (String suffix : properties.getTrustedHostSuffixes()) {
			if (normalized.equals(suffix.toLowerCase()) || normalized.endsWith("." + suffix.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
