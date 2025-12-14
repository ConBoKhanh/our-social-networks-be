package com.oursocialnetworks.component;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.JwtService;
import com.oursocialnetworks.service.SupabaseUserService;
import com.oursocialnetworks.service.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SupabaseUserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy email từ OAuth2
        String email = oAuth2User.getAttribute("email");
        String sub = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if (email == null) {
            redirectToFrontendWithError(response, "Không thể lấy email từ Google");
            return;
        }

        try {
            // Tìm hoặc tạo user trong database
            SupabaseUserService.UserCreationResult result = userService.findOrCreateUser(email);
            User user = result.getUser();
            boolean isNewUser = result.isNewUser();
            String tempPassword = result.getTempPassword();

            if (isNewUser) {
                // USER MỚI - Gửi email, đổi password tự động, và tạo token
                if (tempPassword != null) {
                    // Tự động đổi password từ temp sang password mới
                    String newPassword = generateNewPassword(email); // Tạo password mới
                    
                    // Update password và status trong database
                    updateUserPasswordAndStatus(user, newPassword);
                    
                    // Update local user object
                    user.setPasswordLogin(newPassword);
                    user.setStatus(1); // Active status
                    
                    // Gửi email thông báo tài khoản đã được kích hoạt với password mới
                    emailService.sendAccountActivatedEmail(email, user.getUsername(), newPassword);
                }
                
                // Update thông tin OAuth2
                updateOAuth2Info(user, sub, emailVerified);
                
                // Tạo JWT tokens cho user mới
                String accessToken = jwtService.generateToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                // Redirect về FE với tokens và thông báo
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(frontendUrl + "/auth/callback")
                        .queryParam("accessToken", accessToken)
                        .queryParam("refreshToken", refreshToken)
                        .queryParam("status", "success")
                        .queryParam("isNewUser", "true")
                        .queryParam("message", "Tài khoản mới đã được tạo và kích hoạt thành công! Thông tin đăng nhập đã được gửi qua email.");

                response.sendRedirect(builder.build().toUriString());
                
            } else {
                // USER CŨ - Tạo token và redirect về dashboard
                
                // Update thông tin OAuth2 nếu cần
                updateOAuth2Info(user, sub, emailVerified);
                
                // Tạo JWT tokens
                String accessToken = jwtService.generateToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                // Redirect về FE với tokens
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(frontendUrl + "/auth/callback")
                        .queryParam("accessToken", accessToken)
                        .queryParam("refreshToken", refreshToken)
                        .queryParam("status", "success")
                        .queryParam("message", "Đăng nhập thành công!")
                        .queryParam("userStatus", user.getStatus()); // Để FE biết có cần đổi password không

                response.sendRedirect(builder.build().toUriString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectToFrontendWithError(response, "Đăng nhập thất bại: " + e.getMessage());
        }
    }

    private void updateOAuth2Info(User user, String sub, Boolean emailVerified) {
        try {
            boolean needUpdate = false;
            if (user.getOpenidSub() == null && sub != null) {
                user.setOpenidSub(sub);
                needUpdate = true;
            }
            if (user.getProvider() == null) {
                user.setProvider("google");
                needUpdate = true;
            }
            if (user.getEmailVerified() == null && emailVerified != null) {
                user.setEmailVerified(emailVerified);
                needUpdate = true;
            }

            if (needUpdate) {
                // Update user info in database
                System.out.println("Updating OAuth2 info for user: " + user.getEmail());
                // userService.updateUserById(user.getId(), user, User[].class);
            }
        } catch (Exception e) {
            System.err.println("Failed to update OAuth2 info: " + e.getMessage());
        }
    }

    private String generateNewPassword(String email) {
        // Tạo password mới từ email (hoặc có thể random)
        // Ví dụ: lấy phần trước @ và thêm số random
        String username = email.split("@")[0];
        int randomNum = (int) (Math.random() * 1000);
        return username + randomNum; // Ví dụ: john123
    }
    
    private void updateUserPasswordAndStatus(User user, String newPassword) {
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("password_login", newPassword);
            updateData.put("status", 1); // Change from temporary (2) to active (1)
            updateData.put("updateDate", java.time.LocalDate.now().toString());
            
            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + user.getId());
            
            userService.put("user", params, updateData, User[].class);
            
            System.out.println("Updated password for new user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to update user password: " + e.getMessage());
            throw new RuntimeException("Failed to update user password", e);
        }
    }

    private void redirectToFrontendWithError(HttpServletResponse response, String errorMessage) throws IOException {
        String errorUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/auth/callback")
                .queryParam("status", "error")
                .queryParam("message", errorMessage)
                .build()
                .toUriString();
        response.sendRedirect(errorUrl);
    }
}