package com.sbp.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(SbpLoggingProperties.class)
@ConditionalOnProperty(prefix = "sbp.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({ SbpLoggerImpl.class, LogArchiveService.class, LogArchiveScheduler.class, TomcatAccessLogCustomizer.class })
public class SbpLoggingAutoConfiguration {

}
