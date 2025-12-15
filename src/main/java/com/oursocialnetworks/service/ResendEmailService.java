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
            body.put("subject", "🔐 Mật khẩu tạm thời cho tài khoản ConBoKhanh của bạn");
            body.put("html", htmlContent);
            
            System.out.println("📧 [Resend] From: " + fromAddress);
            
            // Thêm tags để tracking
            Map<String, String> tags = new HashMap<>();
            tags.put("category", "temp-password");
            tags.put("environment", "production");
            body.put("tags", tags);

            // Thêm unique headers để tránh duplicate detection
            Map<String, String> emailHeaders = new HashMap<>();
            String uniqueId = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            emailHeaders.put("X-Entity-Ref-ID", uniqueId);
            emailHeaders.put("X-Request-ID", uniqueId);
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
