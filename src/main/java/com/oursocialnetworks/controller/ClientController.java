package com.oursocialnetworks.controller;

import com.oursocialnetworks.component.AuthUtils;
import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.SupabaseUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client", description = "API cho client app")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final SupabaseUserService userService;
    private final AuthUtils authUtils;

    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin profile user hiện tại")
    public ResponseEntity<?> getProfile() {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            ResponseEntity<User[]> response = userService.getUserById(currentUserId.toString(), User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                User user = response.getBody()[0];
                
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("data", user);
                
                return ResponseEntity.ok(result);
            }
            
            return authUtils.buildErrorResponse("Không tìm thấy thông tin user");
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @PutMapping("/profile")
    @Operation(
        summary = "Cập nhật thông tin profile",
        description = "Cập nhật username, description, place_of_residence, image. Chỉ cập nhật các field được gửi lên."
    )
    public ResponseEntity<?> updateProfile(@RequestBody com.oursocialnetworks.dto.UpdateProfileRequest request) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            
            // Chỉ cho phép update một số field nhất định
            Map<String, Object> allowedUpdates = new HashMap<>();
            if (request.getUsername() != null) {
                allowedUpdates.put("username", request.getUsername());
            }
            if (request.getDescription() != null) {
                allowedUpdates.put("description", request.getDescription());
            }
            if (request.getPlaceOfResidence() != null) {
                allowedUpdates.put("place_of_residence", request.getPlaceOfResidence());
            }
            if (request.getImage() != null) {
                allowedUpdates.put("image", request.getImage());
            }
            
            if (allowedUpdates.isEmpty()) {
                return authUtils.buildErrorResponse("Không có thông tin nào để cập nhật");
            }
            
            ResponseEntity<User[]> response = userService.updateUserById(currentUserId, allowedUpdates, User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "Cập nhật thành công!");
                result.put("data", response.getBody()[0]);
                
                return ResponseEntity.ok(result);
            }
            
            return authUtils.buildErrorResponse("Không thể cập nhật profile");
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/users/search")
    @Operation(summary = "Tìm kiếm user theo username")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                return authUtils.buildErrorResponse("Từ khóa tìm kiếm không được để trống");
            }
            
            ResponseEntity<User[]> response = userService.searchUserByUsername(q.trim(), User[].class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", response.getBody() != null ? response.getBody() : new User[0]);
            result.put("count", response.getBody() != null ? response.getBody().length : 0);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Lấy thông tin user theo ID")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            ResponseEntity<User[]> response = userService.getUserById(id, User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("data", response.getBody()[0]);
                
                return ResponseEntity.ok(result);
            }
            
            return authUtils.buildErrorResponse("Không tìm thấy user");
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/auth/check")
    @Operation(summary = "Kiểm tra JWT token còn hiệu lực không")
    public ResponseEntity<?> checkAuth() {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            ResponseEntity<User[]> response = userService.getUserById(currentUserId.toString(), User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                User user = response.getBody()[0];
                
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("authenticated", true);
                result.put("userId", user.getId());
                result.put("username", user.getUsername());
                result.put("email", user.getEmail());
                
                return ResponseEntity.ok(result);
            }
            
            return authUtils.buildErrorResponse("Token không hợp lệ");
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("authenticated", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(401).body(result);
        }
    }

    @GetMapping("/users/by-username/{username}")
    @Operation(summary = "Lấy thông tin user theo username_login (như Instagram)")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            // Tìm user theo username_login
            Map<String, String> params = new HashMap<>();
            params.put("username_login", "eq." + username);
            params.put("status", "eq.1");
            params.put("select", "*,Role(*)");
            params.put("limit", "1");
            
            ResponseEntity<User[]> response = userService.get("user", params, User[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("data", response.getBody()[0]);
                
                return ResponseEntity.ok(result);
            }
            
            return authUtils.buildErrorResponse("Không tìm thấy user");
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }
}