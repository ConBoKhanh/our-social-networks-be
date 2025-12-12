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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users in the social network")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final SupabaseUserService supabaseService;

    @Operation(
            summary = "Get all active users",
            description = "Retrieve all users with status = 1 (active users only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            ResponseEntity<User[]> response = supabaseService.getAllActiveUsers(User[].class);
            User[] users = response.getBody();

            if (users != null) {
                List<User> userList = Arrays.asList(users);
                ApiResponse<List<User>> apiResponse = ApiResponse.success(
                    "Lấy danh sách người dùng thành công", userList);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<List<User>> apiResponse = ApiResponse.success(
                    "Không có người dùng nào", Arrays.asList());
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<List<User>> apiResponse = ApiResponse.error(
                "Lỗi khi lấy danh sách người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Create new user (Admin only)",
            description = "Create a new user in the system - requires Admin role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            // Set default values
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            if (user.getCreateDate() == null) {
                user.setCreateDate(java.time.LocalDate.now());
            }
            if (user.getUpdateDate() == null) {
                user.setUpdateDate(java.time.LocalDate.now());
            }

            ResponseEntity<User[]> response = supabaseService.createUser(user, User[].class);
            User[] createdUsers = response.getBody();

            if (createdUsers != null && createdUsers.length > 0) {
                User createdUser = createdUsers[0];
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Tạo người dùng thành công", createdUser);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không thể tạo người dùng", 400);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in createUser: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi tạo người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Update user (Admin only)",
            description = "Update an existing user - requires Admin role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            // Set update date
            user.setUpdateDate(java.time.LocalDate.now());

            ResponseEntity<User[]> response = supabaseService.updateUserById(
                java.util.UUID.fromString(id), user, User[].class);
            User[] updatedUsers = response.getBody();

            if (updatedUsers != null && updatedUsers.length > 0) {
                User updatedUser = updatedUsers[0];
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Cập nhật người dùng thành công", updatedUser);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không thể cập nhật người dùng", 400);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in updateUser: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi cập nhật người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Soft delete user (Admin only)",
            description = "Set user status to 0 (inactive) - requires Admin role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> deleteUser(@PathVariable String id) {
        System.out.println("========== DELETE USER DEBUG START ==========");
        System.out.println("Deleting user with ID: " + id);
        
        try {
            System.out.println("Parsing UUID...");
            java.util.UUID userId = java.util.UUID.fromString(id);
            System.out.println("UUID parsed successfully: " + userId);
            
            System.out.println("Calling softDeleteUser...");
            ResponseEntity<User[]> response = supabaseService.softDeleteUser(userId, User[].class);
            System.out.println("Service call completed. Response status: " + response.getStatusCode());
            
            User[] deletedUsers = response.getBody();
            System.out.println("Response body length: " + (deletedUsers != null ? deletedUsers.length : "null"));

            if (deletedUsers != null && deletedUsers.length > 0) {
                User deletedUser = deletedUsers[0];
                System.out.println("User deleted successfully: " + deletedUser.getId());
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Xóa người dùng thành công (status = 0)", deletedUser);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                System.out.println("No user found with ID: " + id);
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không tìm thấy người dùng với ID: " + id, 404);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in deleteUser: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi xóa người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Restore user (Admin only)",
            description = "Set user status to 1 (active) - requires Admin role",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping(value = "/{id}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<User>> restoreUser(@PathVariable String id) {
        System.out.println("========== RESTORE USER DEBUG START ==========");
        System.out.println("Restoring user with ID: " + id);
        
        try {
            System.out.println("Parsing UUID...");
            java.util.UUID userId = java.util.UUID.fromString(id);
            System.out.println("UUID parsed successfully: " + userId);
            
            System.out.println("Calling restoreUser...");
            ResponseEntity<User[]> response = supabaseService.restoreUser(userId, User[].class);
            System.out.println("Service call completed. Response status: " + response.getStatusCode());
            
            User[] restoredUsers = response.getBody();
            System.out.println("Response body length: " + (restoredUsers != null ? restoredUsers.length : "null"));

            if (restoredUsers != null && restoredUsers.length > 0) {
                User restoredUser = restoredUsers[0];
                System.out.println("User restored successfully: " + restoredUser.getId());
                ApiResponse<User> apiResponse = ApiResponse.success(
                    "Khôi phục người dùng thành công (status = 1)", restoredUser);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                System.out.println("No user found with ID: " + id);
                ApiResponse<User> apiResponse = ApiResponse.error(
                    "Không tìm thấy người dùng với ID: " + id, 404);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in restoreUser: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            ApiResponse<User> apiResponse = ApiResponse.error(
                "Lỗi khi khôi phục người dùng: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Test soft delete without auth",
            description = "Test endpoint to debug soft delete functionality"
    )
    @GetMapping(value = "/test-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> testDelete(@PathVariable String id) {
        try {
            System.out.println("========== TEST DELETE DEBUG ==========");
            System.out.println("Testing delete for user ID: " + id);
            
            java.util.UUID userId = java.util.UUID.fromString(id);
            ResponseEntity<User[]> response = supabaseService.softDeleteUser(userId, User[].class);
            
            System.out.println("Test delete completed. Status: " + response.getStatusCode());
            
            ApiResponse<String> apiResponse = ApiResponse.success(
                "Test delete completed. Status: " + response.getStatusCode(), 
                "Response body length: " + (response.getBody() != null ? response.getBody().length : "null"));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        } catch (Exception e) {
            System.err.println("ERROR in testDelete: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<String> apiResponse = ApiResponse.error(
                "Test delete error: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Test restore without auth",
            description = "Test endpoint to debug restore functionality"
    )
    @GetMapping(value = "/test-restore/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> testRestore(@PathVariable String id) {
        try {
            System.out.println("========== TEST RESTORE DEBUG ==========");
            System.out.println("Testing restore for user ID: " + id);
            
            java.util.UUID userId = java.util.UUID.fromString(id);
            ResponseEntity<User[]> response = supabaseService.restoreUser(userId, User[].class);
            
            System.out.println("Test restore completed. Status: " + response.getStatusCode());
            
            ApiResponse<String> apiResponse = ApiResponse.success(
                "Test restore completed. Status: " + response.getStatusCode(), 
                "Response body length: " + (response.getBody() != null ? response.getBody().length : "null"));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        } catch (Exception e) {
            System.err.println("ERROR in testRestore: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<String> apiResponse = ApiResponse.error(
                "Test restore error: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }

    @Operation(
            summary = "Get all deleted users (Admin only)",
            description = "Retrieve all users with status = 0 (deleted users)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/deleted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<User>>> getDeletedUsers() {
        try {
            ResponseEntity<User[]> response = supabaseService.getDeletedUsers(User[].class);
            User[] users = response.getBody();

            if (users != null) {
                List<User> userList = Arrays.asList(users);
                ApiResponse<List<User>> apiResponse = ApiResponse.success(
                    "Lấy danh sách người dùng đã xóa thành công", userList);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            } else {
                ApiResponse<List<User>> apiResponse = ApiResponse.success(
                    "Không có người dùng nào bị xóa", Arrays.asList());
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(apiResponse);
            }
        } catch (Exception e) {
            System.err.println("ERROR in getDeletedUsers: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<List<User>> apiResponse = ApiResponse.error(
                "Lỗi khi lấy danh sách người dùng đã xóa: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiResponse);
        }
    }
}