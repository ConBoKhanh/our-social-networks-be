package com.oursocialnetworks.controller;

import com.oursocialnetworks.dto.AuthResponse;
import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.OtpService;
import com.oursocialnetworks.service.ResendEmailService;
import com.oursocialnetworks.service.SupabaseUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@Tag(name = "Registration", description = "User registration endpoints")
public class RegisterController {

    @Autowired
    private SupabaseUserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private ResendEmailService resendEmailService;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // ============================
    //  POST /auth/register/check-email - Check if email exists
    // ============================
    @Operation(summary = "Check if email exists")
    @PostMapping("/auth/register/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("limit", "1");

            ResponseEntity<User[]> response = userService.get("user", params, User[].class);
            boolean exists = response.getBody() != null && response.getBody().length > 0;

            return ResponseEntity.ok(Map.of(
                "exists", exists,
                "message", exists ? "Email này đã được sử dụng!" : "Email có thể sử dụng."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/register/send-otp - Send OTP for registration
    // ============================
    @Operation(summary = "Send OTP for registration")
    @PostMapping("/auth/register/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendRegisterOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            // Check if email already exists
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("limit", "1");

            ResponseEntity<User[]> response = userService.get("user", params, User[].class);
            if (response.getBody() != null && response.getBody().length > 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email này đã được sử dụng!"));
            }

            // Generate and send OTP
            String otp = otpService.generateOtp(email, "register");
            boolean sent = resendEmailService.sendOtpEmail(email, otp, "register");

            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mã OTP đã được gửi đến email của bạn!"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Không thể gửi email! Vui lòng thử lại sau."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/register/verify-otp - Verify OTP
    // ============================
    @Operation(summary = "Verify OTP for registration")
    @PostMapping("/auth/register/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyRegisterOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email và OTP là bắt buộc"));
        }

        boolean valid = otpService.verifyOtp(email, otp, "register");
        
        return ResponseEntity.ok(Map.of(
            "valid", valid,
            "message", valid ? "Mã OTP hợp lệ!" : "Mã OTP không hợp lệ hoặc đã hết hạn!"
        ));
    }

    // ============================
    //  POST /auth/register/complete - Complete registration
    // ============================
    @Operation(summary = "Complete registration with password")
    @PostMapping("/auth/register/complete")
    @ResponseBody
    public ResponseEntity<AuthResponse> completeRegistration(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String password = request.get("password");
        String username = request.get("username");

        if (email == null || otp == null || password == null) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Thiếu thông tin bắt buộc!"));
        }

        // Verify OTP again
        if (!otpService.verifyOtp(email, otp, "register")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Mã OTP không hợp lệ hoặc đã hết hạn!"));
        }

        try {
            // Get default role
            Map<String, String> roleParams = new HashMap<>();
            roleParams.put("role", "eq.User");
            roleParams.put("status", "eq.1");
            roleParams.put("limit", "1");
            
            ResponseEntity<com.oursocialnetworks.entity.Role[]> roleResponse = 
                userService.get("role", roleParams, com.oursocialnetworks.entity.Role[].class);
            
            UUID roleId = null;
            if (roleResponse.getBody() != null && roleResponse.getBody().length > 0) {
                roleId = roleResponse.getBody()[0].getId();
            }

            // Create username if not provided
            if (username == null || username.trim().isEmpty()) {
                username = email.split("@")[0] + "_" + System.currentTimeMillis();
            }

            // Create new user
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("email", email);
            newUser.put("gmail", email);
            newUser.put("username", username);
            newUser.put("username_login", username);
            newUser.put("password_login", password);
            newUser.put("status", 1); // Active
            newUser.put("provider", "email");
            newUser.put("email_verified", true);
            newUser.put("createDate", LocalDate.now().toString());
            newUser.put("updateDate", LocalDate.now().toString());
            if (roleId != null) {
                newUser.put("role_id", roleId.toString());
            }

            ResponseEntity<User[]> created = userService.post("user", newUser, User[].class);
            
            if (created.getBody() != null && created.getBody().length > 0) {
                otpService.removeOtp(email); // Remove OTP after successful registration
                return ResponseEntity.ok(AuthResponse.success(
                    "Đăng ký tài khoản thành công! Vui lòng đăng nhập.",
                    null, null, null, true, null
                ));
            }

            return ResponseEntity.status(500).body(AuthResponse.error("Không thể tạo tài khoản! Vui lòng thử lại."));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(AuthResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/forgot-password/send-otp - Send OTP for password reset
    // ============================
    @Operation(summary = "Send OTP for password reset")
    @PostMapping("/auth/forgot-password/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendForgotPasswordOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            // Check if email exists
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("limit", "1");

            ResponseEntity<User[]> response = userService.get("user", params, User[].class);
            if (response.getBody() == null || response.getBody().length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email không tồn tại trong hệ thống!"));
            }

            // Generate and send OTP
            String otp = otpService.generateOtp(email, "forgot");
            boolean sent = resendEmailService.sendOtpEmail(email, otp, "forgot");

            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mã OTP đã được gửi đến email của bạn!"
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Không thể gửi email! Vui lòng thử lại sau."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================
    //  POST /auth/forgot-password/verify-otp - Verify OTP for password reset
    // ============================
    @Operation(summary = "Verify OTP for password reset")
    @PostMapping("/auth/forgot-password/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyForgotPasswordOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email và OTP là bắt buộc"));
        }

        boolean valid = otpService.verifyOtp(email, otp, "forgot");
        
        return ResponseEntity.ok(Map.of(
            "valid", valid,
            "message", valid ? "Mã OTP hợp lệ!" : "Mã OTP không hợp lệ hoặc đã hết hạn!"
        ));
    }

    // ============================
    //  POST /auth/forgot-password/reset - Reset password
    // ============================
    @Operation(summary = "Reset password with OTP")
    @PostMapping("/auth/forgot-password/reset")
    @ResponseBody
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Thiếu thông tin bắt buộc!"));
        }

        // Verify OTP
        if (!otpService.verifyOtp(email, otp, "forgot")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Mã OTP không hợp lệ hoặc đã hết hạn!"));
        }

        try {
            // Update password
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("password_login", newPassword);
            updateData.put("updateDate", LocalDate.now().toString());

            Map<String, String> updateParams = new HashMap<>();
            updateParams.put("email", "eq." + email);

            userService.patch("user", updateParams, updateData, User[].class);
            
            otpService.removeOtp(email); // Remove OTP after successful reset

            return ResponseEntity.ok(AuthResponse.success(
                "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.",
                null, null, null, false, null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(AuthResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
