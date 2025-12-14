package com.oursocialnetworks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

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

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public ResendEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.restTemplate = new RestTemplate();
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
            context.setVariable("changePasswordUrl", "https://conbokhanh.io.vn/change-password?email=" + toEmail);

            String htmlContent = templateEngine.process("email-temp-password", context);

            // Gọi Resend API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", fromEmail);
            body.put("to", toEmail);
            body.put("subject", "🔐 Mật khẩu tạm thời - conbokhanh");
            body.put("html", htmlContent);

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
        return resendEnabled && resendApiKey != null && !resendApiKey.trim().isEmpty();
    }
}
