package com.stockalert.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Placeholder for future OAuth2 / Security configuration.
 *
 * When ready, add:
 *   - spring-boot-starter-oauth2-resource-server dependency
 *   - @EnableWebFluxSecurity
 *   - SecurityWebFilterChain bean with JWT validation
 */
@Configuration
public class SecurityConfig {
    // OAuth2 security will be implemented in a future iteration.
    // Currently all routes are open — protect behind VPN/ingress at the infrastructure level.
}
