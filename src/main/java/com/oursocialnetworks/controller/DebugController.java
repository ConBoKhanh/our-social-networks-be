package com.oursocialnetworks.controller;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.SupabaseUserService;
import com.oursocialnetworks.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Tag(name = "Debug", description = "Debug endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class DebugController {

    private final SupabaseUserService supabaseService;

    @Operation(
            summary = "Test direct PUT to Supabase",
            description = "Test PUT operation directly",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping(value = "/test-put/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Object>> testPut(@PathVariable String id) {
        try {
            System.out.println("========== DEBUG TEST PUT ==========");
            System.out.println("Testing PUT with ID: " + id);
            
            // First, let's test a GET to see what fields exist
            Map<String, String> getParams = new HashMap<>();
            getParams.put("id", "eq." + id);
            getParams.put("limit", "1");
            
            ResponseEntity<User[]> getResponse = supabaseService.get("user", getParams, User[].class);
            System.out.println("GET Response status: " + getResponse.getStatusCode());
            if (getResponse.getBody() != null && getResponse.getBody().length > 0) {
                User existingUser = getResponse.getBody()[0];
                System.out.println("Existing user: " + existingUser);
                System.out.println("User ID: " + existingUser.getId());
                System.out.println("User status: " + existingUser.getStatus());
            }
            
            // Test parameters
            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + id);
            
            // Test different field names to see what works
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", 0);
            
            System.out.println("Testing with 'status' field only...");
            
            System.out.println("PUT Params: " + params);
            System.out.println("PUT UpdateData: " + updateData);
            
            // Call PUT method
            ResponseEntity<User[]> response = supabaseService.put("user", params, updateData, User[].class);
            
            System.out.println("PUT Response status: " + response.getStatusCode());
            System.out.println("PUT Response body: " + java.util.Arrays.toString(response.getBody()));
            System.out.println("===================================");
            
            return ResponseEntity.ok(ApiResponse.success("Test PUT completed", response.getBody()));
            
        } catch (Exception e) {
            System.err.println("========== DEBUG ERROR ==========");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=================================");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Test PUT failed: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping(value = "/test-get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Object>> testGet(@PathVariable String id) {
        try {
            System.out.println("========== DEBUG TEST GET ==========");
            System.out.println("Testing GET with ID: " + id);
            
            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + id);
            params.put("limit", "1");
            
            // Try to get raw response as String first
            ResponseEntity<String> rawResponse = supabaseService.get("user", params, String.class);
            
            System.out.println("GET Response status: " + rawResponse.getStatusCode());
            System.out.println("Raw response body: " + rawResponse.getBody());
            
            // Now try with User array
            ResponseEntity<User[]> response = supabaseService.get("user", params, User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                User user = response.getBody()[0];
                System.out.println("User found: " + user);
                System.out.println("User fields:");
                System.out.println("  ID: " + user.getId());
                System.out.println("  Status: " + user.getStatus());
                System.out.println("  Username: " + user.getUsername());
                System.out.println("  Email: " + user.getEmail());
                System.out.println("  CreateDate: " + user.getCreateDate());
                System.out.println("  UpdateDate: " + user.getUpdateDate());
            }
            System.out.println("===================================");
            
            return ResponseEntity.ok(ApiResponse.success("Test GET completed", rawResponse.getBody()));
            
        } catch (Exception e) {
            System.err.println("========== DEBUG GET ERROR ==========");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=====================================");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Test GET failed: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping(value = "/test-all-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Object>> testAllUsers() {
        try {
            System.out.println("========== DEBUG TEST ALL USERS ==========");
            
            Map<String, String> params = new HashMap<>();
            params.put("limit", "5");
            
            // Try to get raw response as String first
            ResponseEntity<String> rawResponse = supabaseService.get("user", params, String.class);
            
            System.out.println("GET All Response status: " + rawResponse.getStatusCode());
            System.out.println("Raw response body: " + rawResponse.getBody());
            System.out.println("==========================================");
            
            return ResponseEntity.ok(ApiResponse.success("Test GET All completed", rawResponse.getBody()));
            
        } catch (Exception e) {
            System.err.println("========== DEBUG GET ALL ERROR ==========");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=========================================");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Test GET All failed: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping(value = "/public-test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> publicTest() {
        try {
            System.out.println("========== PUBLIC TEST ==========");
            
            // Test basic Supabase connection
            Map<String, String> params = new HashMap<>();
            params.put("limit", "1");
            
            ResponseEntity<String> response = supabaseService.get("user", params, String.class);
            
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            System.out.println("=================================");
            
            return ResponseEntity.ok("Test completed. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
            
        } catch (Exception e) {
            System.err.println("========== PUBLIC TEST ERROR ==========");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=======================================");
            
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping(value = "/test-patch/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testPatch(@PathVariable String id) {
        try {
            System.out.println("========== TEST PATCH ==========");
            System.out.println("Testing PATCH with ID: " + id);
            
            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + id);
            
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", 0);
            updateData.put("updateDate", java.time.LocalDate.now().toString());
            
            System.out.println("PATCH Params: " + params);
            System.out.println("PATCH UpdateData: " + updateData);
            
            ResponseEntity<String> response = supabaseService.patch("user", params, updateData, String.class);
            
            System.out.println("PATCH Response status: " + response.getStatusCode());
            System.out.println("PATCH Response body: " + response.getBody());
            System.out.println("===============================");
            
            return ResponseEntity.ok("PATCH test completed. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
            
        } catch (Exception e) {
            System.err.println("========== PATCH TEST ERROR ==========");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================================");
            
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}