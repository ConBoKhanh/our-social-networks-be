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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client API", description = "APIs for regular users (User role)")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final SupabaseUserService supabaseService;

    @Operation(
            summary = "Get current user profile",
            description = "Get current user information from JWT token - requires User role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        try {
            // Lấy user ID từ JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không có quyền truy cập", 401);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }

            String userId = auth.getPrincipal().toString();

            // Lấy thông tin user từ database
            ResponseEntity<User[]> response = supabaseService.getUserById(userId, User[].class);
            User[] users = response.getBody();

            if (users != null && users.length > 0) {
                User user = users[0];
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Lấy thông tin người dùng thành công", user);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không tìm thấy người dùng", 404);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Update current user profile",
            description = "Update current user information - requires User role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> updateCurrentUser(@RequestBody UpdateProfileRequest request) {
        try {
            // Lấy user ID từ JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không có quyền truy cập", 401);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }

            String userId = auth.getPrincipal().toString();

            // Tạo update data (chỉ cho phép update một số field)
            java.util.Map<String, Object> updateData = new java.util.HashMap<>();
            if (request.getUsername() != null) {
                updateData.put("username", request.getUsername());
            }
            if (request.getDescription() != null) {
                updateData.put("description", request.getDescription());
            }
            if (request.getPlaceOfResidence() != null) {
                updateData.put("place_of_residence", request.getPlaceOfResidence());
            }
            if (request.getDateOfBirth() != null) {
                updateData.put("date-of-birth", request.getDateOfBirth().toString());
            }
            if (request.getImage() != null) {
                updateData.put("image", request.getImage());
            }
            updateData.put("updateDate", java.time.LocalDate.now().toString());

            // Update user
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("id", "eq." + userId);
            
            ResponseEntity<User[]> response = supabaseService.put("user", params, updateData, User[].class);
            User[] updatedUsers = response.getBody();

            if (updatedUsers != null && updatedUsers.length > 0) {
                User updatedUser = updatedUsers[0];
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Cập nhật thông tin thành công", updatedUser);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không thể cập nhật thông tin", 400);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in updateCurrentUser: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi cập nhật thông tin: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    // DTO cho update profile request
    public static class UpdateProfileRequest {
        private String username;
        private String description;
        private String placeOfResidence;
        private java.time.LocalDate dateOfBirth;
        private String image;

        public UpdateProfileRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPlaceOfResidence() { return placeOfResidence; }
        public void setPlaceOfResidence(String placeOfResidence) { this.placeOfResidence = placeOfResidence; }

        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }
}