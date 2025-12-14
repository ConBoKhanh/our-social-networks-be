package com.oursocialnetworks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // Hardcode values for simplicity
    private String allowedOrigins = "http://localhost:4200";
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
    private String allowedHeaders = "*";
    private boolean allowCredentials = true;
    private String environment = "local";

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Configure origins based on environment
        if ("local".equals(environment) || "development".equals(environment)) {
            // Allow all origins in development
            config.setAllowedOriginPatterns(Arrays.asList("*"));
        } else {
            // Use specific origins in production
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        // Configure headers
        if ("*".equals(allowedHeaders)) {
            config.setAllowedHeaders(Arrays.asList("*"));
        } else {
            config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // Configure methods
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // Configure credentials
        config.setAllowCredentials(allowCredentials);

        // Expose headers
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));

        // Cache preflight request
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}