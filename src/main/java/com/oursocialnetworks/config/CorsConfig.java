package com.oursocialnetworks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Cho phép tất cả origins (development)
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // ✅ Cho phép tất cả headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // ✅ Cho phép tất cả methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // ✅ Cho phép credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // ✅ Expose headers
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // ✅ Cache preflight request
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}