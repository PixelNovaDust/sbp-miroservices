package com.sbp.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	@Profile("dev")
	SecurityWebFilterChain devSecurityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(ex -> ex.anyExchange().permitAll())
				.build();
	}

	@Bean
	@Profile("!dev")
	SecurityWebFilterChain prodSecurityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(ex -> ex
						.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
						.anyExchange().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
				}))
				.build();
	}

}
