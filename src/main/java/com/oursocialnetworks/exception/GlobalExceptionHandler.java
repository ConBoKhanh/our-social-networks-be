package com.oursocialnetworks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("code", 500);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        // Log lỗi chi tiết cho dev
        System.err.println("=== GLOBAL EXCEPTION ===");
        System.err.println("Path: " + request.getDescription(false));
        System.err.println("Error: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("========================");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Đã xảy ra lỗi: " + ex.getMessage());
        errorResponse.put("error", "Runtime Error");
        errorResponse.put("code", 500);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        // Log lỗi chi tiết cho dev
        System.err.println("=== RUNTIME EXCEPTION ===");
        System.err.println("Path: " + request.getDescription(false));
        System.err.println("Error: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("=========================");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Dữ liệu không hợp lệ: " + ex.getMessage());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("code", 400);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}