package com.oursocialnetworks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ResendEmailService {

    private final TemplateEngine templateEngine;
    private final RestTemplate restTemplate;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${resend.enabled:false}")
    private boolean resendEnabled;

    @Value("${app.backend.url:https://our-social-networks-be.onrender.com}")
    private String backendUrl;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public ResendEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.restTemplate = new RestTemplate();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("========== RESEND EMAIL SERVICE INIT ==========");
        System.out.println("resendEnabled: " + resendEnabled);
        System.out.println("resendApiKey exists: " + (resendApiKey != null && !resendApiKey.trim().isEmpty()));
        System.out.println("resendApiKey length: " + (resendApiKey != null ? resendApiKey.length() : 0));
        System.out.println("fromEmail: " + fromEmail);
        System.out.println("backendUrl: " + backendUrl);
        System.out.println("Service ready: " + isConfigured());
        System.out.println("================================================");
    }

    /**
     * Gửi email mật khẩu tạm thời qua Resend API
     */
    public boolean sendTempPasswordEmail(String toEmail, String username, String tempPassword) {
        if (!resendEnabled || resendApiKey == null || resendApiKey.trim().isEmpty()) {
            System.out.println("📧 Resend is disabled or not configured");
            return false;
        }

        try {
            System.out.println("📧 [Resend] Sending temp password email to: " + toEmail);

            // Tạo HTML content từ template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("email", toEmail);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("changePasswordUrl", backendUrl + "/change-password?email=" + toEmail);

            String htmlContent = templateEngine.process("email-temp-password", context);

            // Gọi Resend API với cấu hình chống spam
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = new HashMap<>();
            // Sử dụng domain từ config, fallback sang onboarding@resend.dev nếu domain chưa verify
            String senderEmail = fromEmail;
            if (senderEmail == null || senderEmail.trim().isEmpty()) {
                senderEmail = "onboarding@resend.dev";
            }
            
            // Format sender với tên hiển thị
            String fromAddress = senderEmail.contains("resend.dev") 
                ? "ConBoKhanh <" + senderEmail + ">"
                : "ConBoKhanh <" + senderEmail + ">";
            
            body.put("from", fromAddress);
            body.put("to", toEmail);
            body.put("subject", "Chào mừng bạn đến với ConBoKhanh");
            body.put("html", htmlContent);
            
            System.out.println("📧 [Resend] From: " + fromAddress);
            
            // Thêm tags để tracking
            Map<String, String> tags = new HashMap<>();
            tags.put("category", "welcome");
            tags.put("environment", "production");
            body.put("tags", tags);
            
            // Thêm reply-to và text version để tránh spam
            body.put("reply_to", "support@conbokhanh.io.vn");
            
            // Thêm text version (quan trọng để tránh spam)
            String textContent = "Chào mừng bạn đến với ConBoKhanh!\n\n" +
                "Tài khoản của bạn đã được tạo thành công.\n" +
                "Email: " + toEmail + "\n" +
                "Mật khẩu đăng nhập: " + tempPassword + "\n\n" +
                "Vui lòng truy cập: " + backendUrl + "/change-password?email=" + toEmail + "\n\n" +
                "Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\nĐội ngũ ConBoKhanh";
            body.put("text", textContent);

            // Thêm headers để tránh spam
            Map<String, String> emailHeaders = new HashMap<>();
            String uniqueId = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            emailHeaders.put("X-Entity-Ref-ID", uniqueId);
            emailHeaders.put("X-Request-ID", uniqueId);
            emailHeaders.put("List-Unsubscribe", "<mailto:unsubscribe@conbokhanh.io.vn>");
            emailHeaders.put("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
            emailHeaders.put("X-Mailer", "ConBoKhanh-System");
            body.put("headers", emailHeaders);
            
            System.out.println("📧 [Resend] Unique ID: " + uniqueId);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                RESEND_API_URL,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [Resend] Email sent successfully to: " + toEmail);
                System.out.println("✅ [Resend] Response: " + response.getBody());
                return true;
            } else {
                System.err.println("❌ [Resend] Failed to send email. Status: " + response.getStatusCode());
                System.err.println("❌ [Resend] Response: " + response.getBody());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ [Resend] Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gửi email OTP
     */
    public boolean sendOtpEmail(String toEmail, String otp, String type) {
        if (!isConfigured()) {
            return false;
        }

        try {
            System.out.println("📧 [Resend] Sending OTP email to: " + toEmail + " (type: " + type + ")");

            String subject = type.equals("register") 
                ? "🔐 Mã xác thực đăng ký tài khoản ConBoKhanh"
                : "🔐 Mã xác thực đặt lại mật khẩu ConBoKhanh";
            
            String title = type.equals("register") 
                ? "Xác thực đăng ký tài khoản"
                : "Đặt lại mật khẩu";
            
            String message = type.equals("register")
                ? "Bạn đang đăng ký tài khoản mới tại ConBoKhanh. Vui lòng nhập mã OTP bên dưới để xác thực email của bạn."
                : "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhập mã OTP bên dưới để tiếp tục.";

            String htmlContent = buildOtpEmailHtml(otp, title, message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = new HashMap<>();
            String senderEmail = fromEmail != null && !fromEmail.trim().isEmpty() ? fromEmail : "onboarding@resend.dev";
            body.put("from", "ConBoKhanh <" + senderEmail + ">");
            body.put("to", toEmail);
            body.put("subject", subject);
            body.put("html", htmlContent);

            Map<String, String> emailHeaders = new HashMap<>();
            String uniqueId = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            emailHeaders.put("X-Entity-Ref-ID", uniqueId);
            body.put("headers", emailHeaders);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(RESEND_API_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ [Resend] OTP email sent to: " + toEmail);
                return true;
            }
            System.err.println("❌ [Resend] Failed: " + response.getBody());
            return false;

        } catch (Exception e) {
            System.err.println("❌ [Resend] Error: " + e.getMessage());
            return false;
        }
    }

    private String buildOtpEmailHtml(String otp, String title, String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #f8f9fa; padding: 40px 20px;'>" +
            "<div style='max-width: 500px; margin: 0 auto; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1);'>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
            "<h1 style='font-family: cursive; font-size: 36px; color: #fff; margin: 0;'>conbokhanh</h1>" +
            "</div>" +
            "<div style='padding: 40px 30px; text-align: center;'>" +
            "<h2 style='color: #1a1a1a; margin-bottom: 16px;'>" + title + "</h2>" +
            "<p style='color: #4a5568; margin-bottom: 32px; line-height: 1.6;'>" + message + "</p>" +
            "<div style='background: #f7fafc; border: 2px solid #e2e8f0; border-radius: 12px; padding: 24px; margin: 24px 0;'>" +
            "<div style='font-size: 14px; color: #718096; margin-bottom: 8px;'>Mã OTP của bạn</div>" +
            "<div style='font-size: 36px; font-weight: 700; color: #667eea; letter-spacing: 8px; font-family: monospace;'>" + otp + "</div>" +
            "</div>" +
            "<p style='color: #a0aec0; font-size: 13px;'>⏱️ Mã có hiệu lực trong 5 phút</p>" +
            "<p style='color: #ed8936; font-size: 13px; margin-top: 20px;'>⚠️ Không chia sẻ mã này với bất kỳ ai!</p>" +
            "</div>" +
            "<div style='background: #2d3748; color: #a0aec0; padding: 20px; text-align: center; font-size: 12px;'>" +
            "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này." +
            "</div></div></body></html>";
    }

    /**
     * Kiểm tra Resend đã được cấu hình chưa
     */
    public boolean isConfigured() {
        boolean configured = resendEnabled && resendApiKey != null && !resendApiKey.trim().isEmpty();
        if (!configured) {
            System.out.println("⚠️ [Resend] NOT configured - resendEnabled: " + resendEnabled + ", apiKey exists: " + (resendApiKey != null && !resendApiKey.trim().isEmpty()));
        }
        return configured;
    }
}
