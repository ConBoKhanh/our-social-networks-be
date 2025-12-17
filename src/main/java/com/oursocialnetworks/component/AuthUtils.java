package com.oursocialnetworks.component;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class để xử lý authentication chung cho các controller
 */
@Component
public class AuthUtils {

    /**
     * Lấy user ID từ JWT token
     */
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            String principal = auth.getPrincipal().toString();
            try {
                return UUID.fromString(principal);
            } catch (IllegalArgumentException e) {
                System.err.println("Cannot parse UUID from principal: " + principal);
            }
        }
        throw new RuntimeException("Không thể xác định user hiện tại!");
    }

    /**
     * Build error response chuẩn
     */
    public ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Build success response chuẩn
     */
    public ResponseEntity<?> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * Build success response với message
     */
    public ResponseEntity<?> buildSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
