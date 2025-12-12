package com.oursocialnetworks.controller;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.JwtService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
@Tag(name = "Token Management", description = "Token utilities")
@SecurityRequirement(name = "Bearer Authentication")
public class TokenController {

    private final JwtService jwtService;
    private final SupabaseUserService userService;

    @Operation(
            summary = "Refresh token with role",
            description = "Get new token with role information from database",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping(value = "/refresh-with-role", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshTokenWithRole() {
        try {
            // Lấy user ID từ token hiện tại
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Không có quyền truy cập", 401));
            }

            String userId = auth.getPrincipal().toString();

            // Lấy thông tin user từ database (bao gồm role)
            ResponseEntity<User[]> response = userService.getUserById(userId, User[].class);
            User[] users = response.getBody();

            if (users == null || users.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy user", 404));
            }

            User user = users[0];

            // Tạo token mới có role
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("refreshToken", newRefreshToken);
            tokenData.put("user", user);
            tokenData.put("message", "Token mới đã được tạo với role từ database");

            return ResponseEntity.ok(ApiResponse.success(
                "Tạo token mới thành công", tokenData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo token: " + e.getMessage(), 500));
        }
    }
}