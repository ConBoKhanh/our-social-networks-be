package com.oursocialnetworks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "Server health monitoring endpoints")
public class HealthCheckController {

    @Operation(summary = "Health check endpoint", description = "Check if server is alive and running")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "our-social-networks-be");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Simple ping", description = "Lightweight ping endpoint")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Operation(summary = "Server info", description = "Get server information")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Our Social Networks API");
        info.put("version", "1.0.0");
        info.put("environment", System.getenv("RENDER") != null ? "production" : "local");
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        return ResponseEntity.ok(info);
    }
}