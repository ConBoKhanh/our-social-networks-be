package com.oursocialnetworks.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import com.oursocialnetworks.dto.TokenRequest;
import com.oursocialnetworks.dto.RefreshTokenRequest;
import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.JwtService;
import com.oursocialnetworks.service.SupabaseUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    // Allow empty default to avoid startup failure if property missing; verify later.
    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${app.frontend.url:https://conbokhanh.io.vn}")
    private String frontendUrl;

    @Autowired
    private SupabaseUserService userService;

    @Autowired
    private JwtService jwtService;

    @Data
    private static class BasicLoginRequest {
        @JsonProperty("username_login")
        private String usernameLogin;
        @JsonProperty("password_login")
        private String passwordLogin;
    }

    // ============================
    //  POST /auth/login - Login with Google ID Token
    // ============================
    @Operation(
            summary = "Login with Google ID Token",
            description = "Authenticate user with Google OAuth2 ID token and receive JWT tokens"
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody TokenRequest request) {
        try {
            // 1. Verify token Google
            GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

            String email = payload.getEmail();

            // 2. Tìm hoặc tạo User trong Supabase
            User user = userService.findOrCreateUser(email);

            // 3. Tạo access & refresh token
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "user", user
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid ID Token", "message", e.getMessage()));
        }
    }

    // ============================
    //  GET /auth/check - Check authentication status
    // ============================
    @Operation(
            summary = "Check authentication status",
            description = "Verify if the current user is authenticated with valid JWT token",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
                String userId = auth.getPrincipal().toString();

                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "userId", userId,
                        "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "authenticated", false,
                                "error", "No authentication found"
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "authenticated", false,
                            "error", "Authentication check failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================
    //  POST /auth/login/basic - Login with username/password
    // ============================
    @Operation(
            summary = "Login with username/password",
            description = "Verify credentials on Supabase and return JWT tokens"
    )
    @PostMapping("/login/basic")
    public ResponseEntity<?> loginWithCredentials(@RequestBody BasicLoginRequest req) {
        try {
            ResponseEntity<User[]> res = userService.loginUser(
                    req.getUsernameLogin(),
                    req.getPasswordLogin(),
                    User[].class
            );

            User[] users = res.getBody();
            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }

            User user = users[0];
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "user", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed", "message", e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/refresh - Refresh access token
    // ============================
    @Operation(
            summary = "Refresh access token",
            description = "Get a new access token using a valid refresh token",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // Verify refresh token
            var claims = jwtService.verify(request.getRefreshToken());
            String userId = claims.getSubject();

            // Get user from database
            User[] users = userService.getUserById(userId, User[].class).getBody();

            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User user = users[0];

            // Generate new access token
            String newAccessToken = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", request.getRefreshToken(),
                    "message", "Token refreshed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Invalid refresh token",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================
    //  POST /auth/logout
    // ============================
    @Operation(
            summary = "Logout",
            description = "Clear authentication context (client should also clear tokens)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of(
                    "message", "Logged out successfully",
                    "note", "Please remove tokens from client side"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed"));
        }
    }

    // ============================
    //  GET /auth/callback - OAuth2 callback page
    // ============================
    @Operation(
            summary = "OAuth2 callback handler",
            description = "Handle OAuth2 callback and redirect to login page with tokens"
    )
    @GetMapping("/callback")
    public String authCallback(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String error
    ) {
        String target = frontendUrl + "/auth/callback";
        if (error != null) {
            return "redirect:" + target + "?error=" + error;
        }
        return "redirect:" + target + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
    }

    // ============================
    //  VERIFY GOOGLE TOKEN
    // ============================
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenStr) throws Exception {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("Google Client ID is not configured");
        }

        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();

        GoogleIdToken idToken = verifier.verify(idTokenStr);

        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new IllegalArgumentException("Invalid Google ID Token");
        }
    }
}