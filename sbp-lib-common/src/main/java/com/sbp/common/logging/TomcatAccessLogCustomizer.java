package com.sbp.common.logging;

import java.io.File;

import org.apache.catalina.valves.AccessLogValve;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(TomcatServletWebServerFactory.class)
@ConditionalOnProperty(prefix = "sbp.logging", name = "access-log-enabled", havingValue = "true", matchIfMissing = true)
public class TomcatAccessLogCustomizer {

	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> sbpAccessLogValve(SbpLoggingProperties properties) {
		return factory -> factory.addContextCustomizers(context -> {
			AccessLogValve valve = new AccessLogValve();
			valve.setDirectory(new File(properties.getDirectory()).getAbsolutePath());
			valve.setPrefix(properties.getAccessLogFileName());
			valve.setSuffix(".txt");
			valve.setPattern("common");
			valve.setRotatable(true);
			valve.setRenameOnRotate(true);
			valve.setFileDateFormat(".yyyy-MM-dd");
			context.getPipeline().addValve(valve);
		});
	}

}
