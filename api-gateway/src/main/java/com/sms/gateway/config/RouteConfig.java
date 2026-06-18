package com.sms.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route configuration (backup to YAML routes).
 * Routes are already defined in application.yml — this class is here for
 * extensibility if custom filters/predicates are needed later.
 */
@Configuration
public class RouteConfig {
    // Routes are configured via application.yml
    // This class can be used for programmatic route customization if needed
}
