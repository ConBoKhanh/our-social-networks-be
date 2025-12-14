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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SupabaseUserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend.url:https://conbokhanh.io.vn}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        System.out.println("========== OAuth2SuccessHandler START ==========");
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy email từ OAuth2
        String email = oAuth2User.getAttribute("email");
        String sub = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        System.out.println("OAuth2 User Info: email=" + email + ", sub=" + sub + ", name=" + name);

        if (email == null) {
            System.err.println("ERROR: Email is null from OAuth2");
            redirectToFrontendWithError(response, "Khong the lay email tu Google");
            return;
        }

        try {
            // Tìm hoặc tạo user trong database
            System.out.println("Finding or creating user for email: " + email);
            SupabaseUserService.UserCreationResult result = userService.findOrCreateUser(email);
            User user = result.getUser();
            boolean isNewUser = result.isNewUser();
            String tempPassword = result.getTempPassword();

            System.out.println("User result: isNewUser=" + isNewUser + ", userId=" + (user != null ? user.getId() : "null"));

            if (isNewUser) {
                // USER MỚI - Gửi email password và redirect đến trang đổi mật khẩu
                System.out.println("NEW USER - Sending temp password email and redirecting to change-password");
                
                if (tempPassword != null) {
                    try {
                        emailService.sendTempPasswordEmail(email, user.getUsername(), tempPassword);
                        System.out.println("Temp password email sent successfully");
                    } catch (Exception emailEx) {
                        System.err.println("Failed to send temp password email: " + emailEx.getMessage());
                        // Continue anyway - user can request password reset later
                    }
                }
                
                // Update thông tin OAuth2 nhưng giữ status = 2 (cần đổi mật khẩu)
                updateOAuth2Info(user, sub, emailVerified);
                
                // Redirect đến trang đổi mật khẩu của BE với thông tin user
                String redirectUrl = "/change-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                        + "&isNewUser=true"
                        + "&message=" + URLEncoder.encode("Tai khoan moi da duoc tao! Vui long kiem tra email de lay mat khau tam thoi.", StandardCharsets.UTF_8);

                System.out.println("Redirecting NEW USER to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
                
            } else {
                // USER CŨ - Tạo token và redirect về FE callback
                System.out.println("EXISTING USER - Generating tokens and redirecting to frontend");
                
                // Update thông tin OAuth2 nếu cần
                updateOAuth2Info(user, sub, emailVerified);
                
                // Tạo JWT tokens
                String accessToken = jwtService.generateToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                String targetUrl = frontendUrl + "/auth/callback";
                
                String redirectUrl = targetUrl 
                        + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                        + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                        + "&status=success"
                        + "&message=" + URLEncoder.encode("Dang nhap thanh cong!", StandardCharsets.UTF_8)
                        + "&userStatus=" + user.getStatus();

                System.out.println("Redirecting EXISTING USER to: " + targetUrl);
                System.out.println("Full redirect URL length: " + redirectUrl.length());
                response.sendRedirect(redirectUrl);
            }

            System.out.println("========== OAuth2SuccessHandler END ==========");

        } catch (Exception e) {
            System.err.println("========== OAuth2SuccessHandler ERROR ==========");
            e.printStackTrace();
            redirectToFrontendWithError(response, "Dang nhap that bai: " + e.getMessage());
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



    private void redirectToFrontendWithError(HttpServletResponse response, String errorMessage) throws IOException {
        String errorUrl = frontendUrl + "/auth/callback"
                + "?status=error"
                + "&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        System.err.println("Redirecting with ERROR to: " + errorUrl);
        response.sendRedirect(errorUrl);
    }
}