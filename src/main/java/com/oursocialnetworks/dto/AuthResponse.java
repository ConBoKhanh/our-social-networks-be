package com.oursocialnetworks.dto;

import com.oursocialnetworks.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String status;           // "success" | "error"
    private String message;          // Thông báo
    private String accessToken;      // JWT access token
    private String refreshToken;     // JWT refresh token
    private User user;              // Thông tin user
    private Boolean isNewUser;      // true nếu user mới được tạo
    private String tempPassword;    // Password tạm thời (chỉ cho user mới)
    
    // Constructor cho success
    public static AuthResponse success(String message, String accessToken, String refreshToken, User user, Boolean isNewUser, String tempPassword) {
        return new AuthResponse("success", message, accessToken, refreshToken, user, isNewUser, tempPassword);
    }
    
    // Constructor cho error
    public static AuthResponse error(String message) {
        return new AuthResponse("error", message, null, null, null, false, null);
    }
}