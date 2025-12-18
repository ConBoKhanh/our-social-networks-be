package com.oursocialnetworks.dto;

import com.oursocialnetworks.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String status;                  // "success" | "error"
    private String message;                 // Thông báo
    private String accessToken;             // JWT access token
    private String refreshToken;            // JWT refresh token
    private User user;                      // Thông tin user
    private Boolean isNewUser;              // true nếu user mới được tạo
    private String tempPassword;            // Password tạm thời (chỉ cho user mới)
    private Boolean requirePasswordChange;  // true nếu user cần đổi mật khẩu (status = 2)
    
    // Constructor cho success
    public static AuthResponse success(String message, String accessToken, String refreshToken, User user, Boolean isNewUser, String tempPassword) {
        Boolean requirePasswordChange = (user != null && user.getStatus() == 2);
        return new AuthResponse("success", message, accessToken, refreshToken, user, isNewUser, tempPassword, requirePasswordChange);
    }
    
    // Constructor cho error
    public static AuthResponse error(String message) {
        return new AuthResponse("error", message, null, null, null, false, null, false);
    }
}