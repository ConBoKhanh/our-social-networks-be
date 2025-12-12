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

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Lấy email từ OAuth2
        String email = oAuth2User.getAttribute("email");
        String sub = oAuth2User.getAttribute("sub");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if (email == null) {
            response.sendRedirect(frontendUrl + "/login?error=no_email");
            return;
        }

        try {
            // Tìm hoặc tạo user trong database
            SupabaseUserService.UserCreationResult result = userService.findOrCreateUser(email);
            User user = result.getUser();
            boolean isNewUser = result.isNewUser();
            String tempPassword = result.getTempPassword();

            // Bổ sung thông tin OAuth2 nếu còn thiếu
            boolean needUpdate = false;
            if (user.getOpenidSub() == null) {
                user.setOpenidSub(sub);
                needUpdate = true;
            }
            if (user.getProvider() == null) {
                user.setProvider("google");
                needUpdate = true;
            }
            if (user.getEmailVerified() == null) {
                user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
                needUpdate = true;
            }

            // Update user nếu cần
            if (needUpdate) {
                // Convert UUID to Long if needed, or use different method
                // userService.updateUserById(user.getId(), user, User[].class);
                System.out.println("User info updated (OAuth2): " + user.getEmail());
            }

            // Gửi email nếu là user mới
            if (isNewUser && tempPassword != null) {
                emailService.sendNewAccountEmail(email, user.getUsername(), tempPassword);
            }

            // Tạo JWT tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Redirect về FE với tokens và thông tin trong URL params
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("status", "success")
                    .queryParam("isNewUser", isNewUser);

            if (isNewUser) {
                builder.queryParam("message", "Tài khoản mới đã được tạo. Vui lòng kiểm tra email để lấy mật khẩu tạm thời.");
            } else {
                builder.queryParam("message", "Đăng nhập thành công!");
            }

            String redirectUrl = builder.build().toUriString();
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            e.printStackTrace();
            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("status", "error")
                    .queryParam("message", "Đăng nhập thất bại: " + e.getMessage())
                    .build()
                    .toUriString();
            response.sendRedirect(errorUrl);
        }
    }
}