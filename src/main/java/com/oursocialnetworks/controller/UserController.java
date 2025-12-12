package com.oursocialnetworks.controller;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.SupabaseUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Data;
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
@SecurityRequirement(name = "Bearer Authentication")  // ✅ THÊM: Yêu cầu JWT cho tất cả endpoints
public class UserController {

    private final SupabaseUserService supabaseService;

    @Operation(
            summary = "Get all active users",
            description = """
                    Retrieve all users with status = 1 (active users only). Limited to 100 users for performance.
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            ResponseEntity<User[]> response = supabaseService.getAllActiveUsers(User[].class);

            System.out.println("========== GET ALL USERS ==========");
            System.out.println("Status: " + response.getStatusCode());

            User[] users = response.getBody();

            if (users != null) {
                System.out.println("Total Users: " + users.length);
                List<User> userList = Arrays.asList(users);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(userList);
            } else {
                System.out.println("Body = NULL");
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Arrays.asList());
            }
        } catch (Exception e) {
            System.err.println("ERROR in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }


    @Operation(
            summary = "Get user by ID",
            description = """
                    Retrieve a specific user by their ID (only if status = 1)
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", example = "138f2c87-d94c-41a1-95d6-b1e9e05e9d4e", required = true)
            @PathVariable String id
    ) {
        try {
            ResponseEntity<User[]> response = supabaseService.getUserById(id, User[].class);
            User[] users = response.getBody();

            if (users != null && users.length > 0) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(users[0]);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        }
    }


    @Operation(
            summary = "Search users by username",
            description = """
                    Search for users whose username contains the specified text (case-insensitive). Limited to 50 results.
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> searchByUsername(
            @Parameter(description = "Username to search for", example = "john", required = true)
            @RequestParam String username
    ) {
        try {
            ResponseEntity<User[]> response = supabaseService.searchUserByUsername(username, User[].class);
            User[] users = response.getBody();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "Get deleted users",
            description = """
                    Retrieve all users with status = 0 (soft deleted users). Limited to 50 results.
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deleted users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/deleted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getDeletedUsers() {
        try {
            ResponseEntity<User[]> response = supabaseService.getDeletedUsers(User[].class);
            User[] users = response.getBody();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "Create new user",
            description = """
                    Create a new user account. Status will be automatically set to 1 (active)
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User object to be created",
                    required = true
            )
            @RequestBody User user
    ) {
        try {
            user.setStatus(1);
            user.setCreateDate(java.time.OffsetDateTime.now());
            user.setUpdateDate(java.time.OffsetDateTime.now());

            ResponseEntity<User[]> response = supabaseService.createUser(user, User[].class);
            User[] users = response.getBody();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "User login",
            description = """
                    Authenticate user with username and password
                    
                    ⚠️ **DEPRECATED:** Use /auth/login with Google OAuth2 instead
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true
            )
            @RequestBody LoginRequest loginRequest
    ) {
        try {
            ResponseEntity<User[]> response = supabaseService.loginUser(
                    loginRequest.getUsername_login(),
                    loginRequest.getPassword_login(),
                    User[].class
            );

            User[] users = response.getBody();

            if (users != null && users.length > 0) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Arrays.asList(users));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Arrays.asList());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "Update user",
            description = """
                    Update user information by ID. UpdateDate will be automatically set to current date
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> updateUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user object",
                    required = true
            )
            @RequestBody User user
    ) {
        try {
            user.setUpdateDate(java.time.OffsetDateTime.now());
            ResponseEntity<User[]> response = supabaseService.updateUserById(id, user, User[].class);
            User[] users = response.getBody();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "Soft delete user",
            description = """
                    Mark user as deleted by setting status = 0 (user data is not physically deleted)
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> deleteUser(
            @Parameter(description = "User ID to delete", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            ResponseEntity<User[]> response = supabaseService.softDeleteUser(id, User[].class);
            User[] users = response.getBody();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    @Operation(
            summary = "Restore deleted user",
            description = """
                    Restore a soft-deleted user by setting status back to 1
                    
                    **Authentication Required:** Bearer token in Authorization header
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{id}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> restoreUser(
            @Parameter(description = "User ID to restore", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            ResponseEntity<User[]> response = supabaseService.restoreUser(id, User[].class);
            User[] users = response.getBody();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(users != null ? Arrays.asList(users) : Arrays.asList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Arrays.asList());
        }
    }

    /**
     * DTO class for login request
     */
    @Data
    public static class LoginRequest {
        @io.swagger.v3.oas.annotations.media.Schema(
                description = "Username for login",
                example = "conbokhanh",
                requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
        )
        private String username_login;

        @io.swagger.v3.oas.annotations.media.Schema(
                description = "Password for login",
                example = "Duytunbua2003",
                requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
        )
        private String password_login;
    }
}