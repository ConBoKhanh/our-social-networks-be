package com.oursocialnetworks.controller;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.SupabaseUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "*",
        maxAge = 3600,
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
@Tag(name = "User Management", description = "APIs for managing users in the social network")
public class UserController {

    private final SupabaseUserService supabaseService;

    @Operation(
            summary = "Get all active users",
            description = "Retrieve all users with status = 1 (active users only). Limited to 100 users for performance."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<User[]> getAllUsers() {
        return supabaseService.getAllActiveUsers(User[].class);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by their ID (only if status = 1)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User[]> getUserById(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        return supabaseService.getUserById(id, User[].class);
    }

    @Operation(
            summary = "Search users by username",
            description = "Search for users whose username contains the specified text (case-insensitive). Limited to 50 results."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<User[]> searchByUsername(
            @Parameter(description = "Username to search for", example = "john", required = true)
            @RequestParam String username
    ) {
        return supabaseService.searchUserByUsername(username, User[].class);
    }

    @Operation(
            summary = "Get deleted users",
            description = "Retrieve all users with status = 0 (soft deleted users). Limited to 50 results."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved deleted users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/deleted")
    public ResponseEntity<User[]> getDeletedUsers() {
        return supabaseService.getDeletedUsers(User[].class);
    }

    @Operation(
            summary = "Create new user",
            description = "Create a new user account. Status will be automatically set to 1 (active)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<User[]> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User object to be created",
                    required = true
            )
            @RequestBody User user
    ) {
        user.setStatus(1);
        user.setCreateDate(java.time.OffsetDateTime.now());
        user.setUpdateDate(java.time.OffsetDateTime.now());
        return supabaseService.createUser(user, User[].class);
    }

    @Operation(
            summary = "User login",
            description = "Authenticate user with username and password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<User[]> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true
            )
            @RequestBody LoginRequest loginRequest
    ) {
        return supabaseService.loginUser(
                loginRequest.getUsername_login(),
                loginRequest.getPassword_login(),
                User[].class
        );
    }

    @Operation(
            summary = "Update user",
            description = "Update user information by ID. UpdateDate will be automatically set to current date"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User[]> updateUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user object",
                    required = true
            )
            @RequestBody User user
    ) {
        user.setUpdateDate(java.time.OffsetDateTime.now());
        return supabaseService.updateUserById(id, user, User[].class);
    }

    @Operation(
            summary = "Soft delete user",
            description = "Mark user as deleted by setting status = 0 (user data is not physically deleted)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<User[]> deleteUser(
            @Parameter(description = "User ID to delete", example = "1", required = true)
            @PathVariable Long id
    ) {
        return supabaseService.softDeleteUser(id, User[].class);
    }

    @Operation(
            summary = "Restore deleted user",
            description = "Restore a soft-deleted user by setting status back to 1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User restored successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/restore")
    public ResponseEntity<User[]> restoreUser(
            @Parameter(description = "User ID to restore", example = "1", required = true)
            @PathVariable Long id
    ) {
        return supabaseService.restoreUser(id, User[].class);
    }

    /**
     * DTO class for login request
     */
    @Data
    public static class LoginRequest {
        @io.swagger.v3.oas.annotations.media.Schema(
                description = "Username for login",
                example = "conbokhanh",
                required = true
        )
        private String username_login;

        @io.swagger.v3.oas.annotations.media.Schema(
                description = "Password for login",
                example = "Duytunbua2003",
                required = true
        )
        private String password_login;
    }
}