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
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        System.out.println("========== OAuth2SuccessHandler INIT ==========");
        System.out.println("Frontend URL configured: " + frontendUrl);
        System.out.println("===============================================");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        System.out.println("========== OAuth2SuccessHandler START ==========");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Request Headers: ");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
            System.out.println("  " + headerName + ": " + request.getHeader(headerName)));
        System.out.println("Authentication: " + authentication.getClass().getSimpleName());
        System.out.println("Response committed at start: " + response.isCommitted());
        
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

            System.out.println("User result: isNewUser=" + isNewUser + ", userId=" + (user != null ? user.getId() : "null") + ", status=" + (user != null ? user.getStatus() : "null"));

            // Kiểm tra status - null hoặc 2 đều cần đổi mật khẩu
            Integer userStatus = user.getStatus();
            boolean needChangePassword = isNewUser || userStatus == null || userStatus == 2;
            
            System.out.println("Check: isNewUser=" + isNewUser + ", userStatus=" + userStatus + ", needChangePassword=" + needChangePassword);
            
            if (needChangePassword) {
                // USER MỚI HOẶC USER CẦN ĐỔI MẬT KHẨU
                System.out.println("========== EMAIL SENDING LOGIC ==========");
                System.out.println("NEW USER OR STATUS=2 USER - Redirect first, then send email in background");
                System.out.println("User status: " + userStatus + ", isNewUser: " + isNewUser);
                System.out.println("tempPassword: " + (tempPassword != null ? "EXISTS" : "NULL"));
                System.out.println("user.getPasswordLogin(): " + (user.getPasswordLogin() != null ? "EXISTS" : "NULL"));
                System.out.println("==========================================");
                
                // Update thông tin OAuth2 nhưng giữ status = 2 (cần đổi mật khẩu)
                updateOAuth2Info(user, sub, emailVerified);
                
                // REDIRECT TRƯỚC - không chờ email (KHÔNG truyền password qua URL - bảo mật)
                String changePasswordUrl = "/change-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                        + "&isNewUser=" + isNewUser
                        + "&message=" + URLEncoder.encode(
                            isNewUser ? "Tai khoan moi da duoc tao! Kiem tra email de lay mat khau tam thoi." 
                                     : "Vui long doi mat khau de tiep tuc.", StandardCharsets.UTF_8);
                
                String processingUrl = "/processing?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                        + "&isNewUser=" + isNewUser
                        + "&redirectUrl=" + URLEncoder.encode(changePasswordUrl, StandardCharsets.UTF_8);

                System.out.println("Redirecting USER to processing page FIRST: " + processingUrl);
                
                if (!response.isCommitted()) {
                    response.sendRedirect(processingUrl);
                    response.flushBuffer();
                    System.out.println("✅ Redirect sent successfully!");
                    
                    // GỬI EMAIL SAU KHI ĐÃ REDIRECT (trong background thread)
                    // Gửi email cho cả user mới và user cũ có status = 2
                    if (tempPassword != null || (userStatus != null && userStatus == 2)) {
                        final String finalEmail = email;
                        final String finalUsername = user.getUsername();
                        final String finalTempPassword = tempPassword != null ? tempPassword : user.getPasswordLogin();
                        
                        new Thread(() -> {
                            try {
                                System.out.println("📧 [BACKGROUND] Sending temp password email after redirect...");
                                System.out.println("📧 [BACKGROUND] isNewUser: " + isNewUser + ", userStatus: " + userStatus);
                                System.out.println("📧 [BACKGROUND] tempPassword: " + (finalTempPassword != null ? "***" : "null"));
                                
                                boolean sent = emailService.sendTempPasswordEmail(finalEmail, finalUsername, finalTempPassword);
                                if (!sent) {
                                    // Log password nếu email fail (để admin hỗ trợ)
                                    System.out.println("=== EMAIL FAILED - TEMP PASSWORD FOR ADMIN SUPPORT ===");
                                    System.out.println("Email: " + finalEmail);
                                    System.out.println("Temp Password: " + finalTempPassword);
                                    System.out.println("isNewUser: " + isNewUser);
                                    System.out.println("userStatus: " + userStatus);
                                    System.out.println("======================================================");
                                }
                            } catch (Exception emailEx) {
                                System.err.println("📧 [BACKGROUND] Failed to send email: " + emailEx.getMessage());
                                emailEx.printStackTrace();
                            }
                        }).start();
                    } else {
                        System.out.println("📧 [SKIP] No email needed - tempPassword: " + (tempPassword != null) + ", userStatus: " + userStatus);
                    }
                } else {
                    System.err.println("ERROR: Response already committed, cannot redirect!");
                }
                
            } else {
                // USER CŨ - Tạo token và redirect về FE callback
                System.out.println("EXISTING USER - Generating tokens and redirecting to frontend");
                System.out.println("Frontend URL from config: " + frontendUrl);
                
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
                System.out.println("Response committed before existing user redirect: " + response.isCommitted());
                response.sendRedirect(redirectUrl);
                response.flushBuffer(); // Ensure redirect is sent immediately
                System.out.println("Existing user redirect sent and flushed!");
            }

            System.out.println("========== OAuth2SuccessHandler END ==========");

        } catch (Exception e) {
            System.err.println("========== OAuth2SuccessHandler ERROR ==========");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("================================================");
            
            // Redirect với thông báo lỗi chi tiết hơn
            String errorMsg = "Lỗi xử lý đăng nhập: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("role")) {
                errorMsg = "Lỗi phân quyền. Vui lòng liên hệ admin.";
            }
            redirectToFrontendWithError(response, errorMsg);
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
        System.err.println("Response committed before error redirect: " + response.isCommitted());
        response.sendRedirect(errorUrl);
        response.flushBuffer(); // Ensure redirect is sent immediately
        System.err.println("Error redirect sent and flushed!");
    }
}