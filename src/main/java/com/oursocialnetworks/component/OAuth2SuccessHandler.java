package com.oursocialnetworks.component;

import com.oursocialnetworks.entity.User;
import com.oursocialnetworks.service.JwtService;
import com.oursocialnetworks.service.SupabaseUserService;
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
            User user = userService.findOrCreateUser(email);

            // Bổ sung thông tin nếu còn thiếu
            if (user.getOpenidSub() == null) {
                user.setOpenidSub(sub);
            }
            if (user.getProvider() == null) {
                user.setProvider("google");
            }
            if (user.getEmailVerified() == null) {
                user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
            }
            if (user.getGmail() == null) {
                user.setGmail(email);
            }
            if (user.getEmail() == null) {
                user.setEmail(email);
            }

            // Tạo JWT tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Redirect về FE với tokens trong URL params
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(frontendUrl + "/login?error=auth_failed");
        }
    }
}