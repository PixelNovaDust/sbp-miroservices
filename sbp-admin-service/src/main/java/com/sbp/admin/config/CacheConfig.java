package com.sbp.admin.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

	@Bean
	CacheManager cacheManager(MonitoringProperties properties) {
		CaffeineCacheManager manager = new CaffeineCacheManager("monitoringSnapshot");
		manager.setCaffeine(Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofSeconds(properties.getCacheTtlSeconds())));
		return manager;
	}

}
