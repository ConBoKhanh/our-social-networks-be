package com.oursocialnetworks.controller;

import com.oursocialnetworks.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test-role")
@Tag(name = "Test Role", description = "Test role-based access")
@SecurityRequirement(name = "Bearer Authentication")
public class TestRoleController {

    @Operation(
            summary = "Test current user info and roles",
            description = "Check what roles the current JWT token has",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> info = new HashMap<>();
        info.put("userId", auth.getPrincipal().toString());
        info.put("authorities", auth.getAuthorities().toString());
        info.put("isAuthenticated", auth.isAuthenticated());
        info.put("name", auth.getName());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(
            "Thông tin user hiện tại", info);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Test USER role access",
            description = "Only users with USER role can access this",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/user-only", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> testUserRole() {
        ApiResponse<String> response = ApiResponse.success(
            "Bạn có quyền USER!", "USER role access successful");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Test ADMIN role access",
            description = "Only users with ADMIN role can access this",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/admin-only", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> testAdminRole() {
        ApiResponse<String> response = ApiResponse.success(
            "Bạn có quyền ADMIN!", "ADMIN role access successful");
        return ResponseEntity.ok(response);
    }
}