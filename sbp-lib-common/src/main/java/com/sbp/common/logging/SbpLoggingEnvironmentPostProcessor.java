package com.sbp.common.logging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Applies default Logback config when SBP logging is enabled.
 */
public class SbpLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String LOGGING_CONFIG = "logging.config";

	private static final String DEFAULT_LOGBACK = "classpath:logback-spring-sbp.xml";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		boolean enabled = environment.getProperty("sbp.logging.enabled", Boolean.class, true);
		if (!enabled) {
			return;
		}
		if (environment.containsProperty(LOGGING_CONFIG)) {
			return;
		}
		Map<String, Object> props = new HashMap<>();
		props.put(LOGGING_CONFIG, DEFAULT_LOGBACK);
		environment.getPropertySources().addFirst(new MapPropertySource("sbpLoggingDefaults", props));
	}

}
