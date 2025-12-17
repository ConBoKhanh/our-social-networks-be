package com.oursocialnetworks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    // Default values
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
    private String allowedHeaders = "*";
    private boolean allowCredentials = true;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Configure origins - allow both production and development
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", 
            "https://localhost:*",
            frontendUrl,
            "https://conbokhanh.io.vn"
        ));

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