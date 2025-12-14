package com.oursocialnetworks.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import com.oursocialnetworks.dto.TokenRequest;
import com.oursocialnetworks.dto.RefreshTokenRequest;
import com.oursocialnetworks.dto.AuthResponse;
import com.oursocialnetworks.dto.ChangePasswordRequest;
import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.JwtService;
import com.oursocialnetworks.service.SupabaseUserService;
import com.oursocialnetworks.service.EmailService;

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
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    // Allow empty default to avoid startup failure if property missing; verify later.
    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private SupabaseUserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

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
    public ResponseEntity<AuthResponse> login(@RequestBody TokenRequest request) {
        try {
            // 1. Verify token Google
            GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());
            String email = payload.getEmail();

            // 2. Tìm hoặc tạo User trong Supabase
            SupabaseUserService.UserCreationResult result = userService.findOrCreateUser(email);
            User user = result.getUser();
            boolean isNewUser = result.isNewUser();
            String tempPassword = result.getTempPassword();

            // 3. Gửi email nếu là user mới
            if (isNewUser && tempPassword != null) {
                emailService.sendNewAccountEmail(email, user.getUsername(), tempPassword);
            }

            // 4. Tạo access & refresh token
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // 5. Tạo response message
            String message = isNewUser 
                ? "Tài khoản mới đã được tạo. Vui lòng kiểm tra email để lấy mật khẩu tạm thời."
                : "Đăng nhập thành công!";

            AuthResponse response = AuthResponse.success(
                message, accessToken, refreshToken, user, isNewUser, 
                isNewUser ? tempPassword : null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Đăng nhập thất bại: " + e.getMessage()));
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
    public ResponseEntity<AuthResponse> loginWithCredentials(@RequestBody BasicLoginRequest req) {
        try {
            ResponseEntity<User[]> res = userService.loginUser(
                    req.getUsernameLogin(),
                    req.getPasswordLogin(),
                    User[].class
            );

            User[] users = res.getBody();
            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Tên đăng nhập hoặc mật khẩu không đúng"));
            }

            User user = users[0];
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            String message = user.getStatus() == 2 
                ? "Đăng nhập thành công! Vui lòng đổi mật khẩu để tiếp tục sử dụng."
                : "Đăng nhập thành công!";

            AuthResponse response = AuthResponse.success(
                message, accessToken, refreshToken, user, false, null
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Đăng nhập thất bại: " + e.getMessage()));
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
    //  POST /auth/change-password-new-user - Change password for new users (no auth required)
    // ============================
    @Operation(
            summary = "Change password for new users",
            description = "Change temporary password for new users (no authentication required)"
    )
    @PostMapping("/change-password-new-user")
    public ResponseEntity<AuthResponse> changePasswordNewUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String tempPassword = request.get("tempPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (email == null || tempPassword == null || newPassword == null || confirmPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Thiếu thông tin bắt buộc"));
            }

            // Validate new password confirmation
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Xác nhận mật khẩu không khớp"));
            }

            // Find user by email and verify temp password
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("password_login", "eq." + tempPassword);
            params.put("status", "eq.2"); // Only users with temporary password
            params.put("select", "*,Role(*)");
            params.put("limit", "1");

            ResponseEntity<User[]> userResponse = userService.get("user", params, User[].class);
            User[] users = userResponse.getBody();
            
            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Email hoặc mật khẩu tạm thời không đúng"));
            }

            User user = users[0];

            // Check if user has temporary password status
            if (user.getStatus() != 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Tài khoản này không cần đổi mật khẩu tạm thời"));
            }

            // Update password and status
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("password_login", newPassword);
            updateData.put("status", 1); // Change from temporary (2) to active (1)
            updateData.put("updateDate", java.time.LocalDate.now().toString());
            
            Map<String, String> updateParams = new HashMap<>();
            updateParams.put("id", "eq." + user.getId());
            
            userService.put("user", updateParams, updateData, User[].class);

            return ResponseEntity.ok(AuthResponse.success(
                "Đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới.",
                null, null, null, false, null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Đổi mật khẩu thất bại: " + e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/change-password - Change password for status = 2 users
    // ============================
    @Operation(
            summary = "Change password",
            description = "Change password for users with status = 2 (temporary password)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Không có quyền truy cập"));
            }

            String userId = auth.getPrincipal().toString();

            // Get current user
            ResponseEntity<User[]> userResponse = userService.getUserById(userId, User[].class);
            User[] users = userResponse.getBody();
            
            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(AuthResponse.error("Không tìm thấy user"));
            }

            User user = users[0];

            // Validate current password
            if (!user.getPasswordLogin().equals(request.getCurrentPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Mật khẩu hiện tại không đúng"));
            }

            // Validate new password confirmation
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Xác nhận mật khẩu không khớp"));
            }

            // Update password and status - use direct field access
            // user.setPasswordLogin(request.getNewPassword());
            // user.setStatus(1); // Change status from 2 to 1 (active)
            // user.setUpdateDate(java.time.LocalDate.now());

            // Save to database - Create update payload manually
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("password_login", request.getNewPassword());
            updateData.put("status", 1);
            updateData.put("updateDate", java.time.LocalDate.now().toString());
            
            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + userId);
            
            userService.put("user", params, updateData, User[].class);
            
            // Update local user object for token generation
            user.setPasswordLogin(request.getNewPassword());
            user.setStatus(1);

            // Generate new tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            AuthResponse response = AuthResponse.success(
                "Đổi mật khẩu thành công! Tài khoản đã được kích hoạt.",
                accessToken, refreshToken, user, false, null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Đổi mật khẩu thất bại: " + e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/login/after-password-change - Login sau khi đổi password
    // ============================
    @Operation(
            summary = "Login after password change",
            description = "Login with new password after changing from temporary password"
    )
    @PostMapping("/login/after-password-change")
    public ResponseEntity<AuthResponse> loginAfterPasswordChange(@RequestBody BasicLoginRequest req) {
        try {
            ResponseEntity<User[]> res = userService.loginUser(
                    req.getUsernameLogin(),
                    req.getPasswordLogin(),
                    User[].class
            );

            User[] users = res.getBody();
            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Tên đăng nhập hoặc mật khẩu không đúng"));
            }

            User user = users[0];
            
            // Check if user still has temporary password status
            if (user.getStatus() == 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(AuthResponse.error("Vui lòng đổi mật khẩu tạm thời trước khi đăng nhập"));
            }

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            AuthResponse response = AuthResponse.success(
                "Đăng nhập thành công! Chào mừng bạn quay lại.", 
                accessToken, refreshToken, user, false, null
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Đăng nhập thất bại: " + e.getMessage()));
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