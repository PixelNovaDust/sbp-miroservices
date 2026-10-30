package com.sbp.studio.config;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatContextRedirectConfig {

	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> disableTomcatContextRootRedirect() {
		return factory -> factory.addContextCustomizers(
				context -> context.setMapperContextRootRedirectEnabled(false));
	}

}
