package com.oursocialnetworks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ResendEmailService resendEmailService;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:}")
    private String emailUsername;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, ResendEmailService resendEmailService) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.resendEmailService = resendEmailService;
    }
    
    /**
     * Gửi email thông báo tài khoản mới được tạo với password tạm thời (BACKGROUND)
     */
    public void sendNewAccountEmail(String email, String username, String tempPassword) {
        // Gửi email trong thread riêng - KHÔNG BLOCK
        new Thread(() -> {
            try {
                System.out.println("📧 [BACKGROUND] Sending new account email to: " + email);
                
                String subject = "🎉 Tài khoản conbokhanh của bạn đã được tạo";
                
                Context context = new Context();
                context.setVariable("username", username);
                context.setVariable("email", email);
                context.setVariable("tempPassword", tempPassword);
                
                String htmlContent = templateEngine.process("email-new-account", context);
                
                sendHtmlEmailInternal(email, subject, htmlContent);
                
            } catch (Exception e) {
                System.err.println("Failed to send new account email to " + email + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Gửi email mật khẩu tạm thời cho user mới từ Google OAuth2
     * Ưu tiên sử dụng Resend API, fallback sang SMTP nếu Resend không available
     */
    public boolean sendTempPasswordEmail(String email, String username, String tempPassword) {
        try {
            System.out.println("📧 Sending temp password email to: " + email);
            
            // Ưu tiên sử dụng Resend API (works on Render)
            if (resendEmailService.isConfigured()) {
                System.out.println("📧 Using Resend API...");
                boolean result = resendEmailService.sendTempPasswordEmail(email, username, tempPassword);
                if (result) {
                    System.out.println("✅ Email sent via Resend API");
                    return true;
                }
                System.out.println("⚠️ Resend failed, trying SMTP fallback...");
            }
            
            // Fallback sang SMTP
            String subject = "🔐 Mật khẩu tạm thời - conbokhanh";
            
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("email", email);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("changePasswordUrl", "https://conbokhanh.io.vn/change-password?email=" + email);
            
            String htmlContent = templateEngine.process("email-temp-password", context);
            
            boolean result = sendHtmlEmailInternal(email, subject, htmlContent);
            
            System.out.println("📧 Email send result for " + email + ": " + (result ? "SUCCESS" : "FAILED"));
            return result;
            
        } catch (Exception e) {
            System.err.println("📧 Failed to send temp password email to " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Internal method để gửi HTML email
     * Nếu SMTP fail (Render blocks), sẽ log password để user có thể sử dụng
     */
    private boolean sendHtmlEmailInternal(String toEmail, String subject, String htmlContent) {
        try {
            // Kiểm tra nếu email bị tắt hoặc chưa config
            if (!emailEnabled || emailUsername == null || emailUsername.trim().isEmpty()) {
                System.out.println("=== EMAIL DISABLED - SKIPPING ===");
                return true;
            }

            System.out.println("📧 Attempting to send HTML email to: " + toEmail);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
            System.out.println("✅ HTML Email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            // SMTP failed - likely Render.com blocking ports
            System.err.println("❌ SMTP Failed (Render blocks SMTP ports): " + e.getMessage());
            System.err.println("💡 Solution: Use SendGrid/Mailgun API instead of SMTP");
            System.err.println("📋 Email was NOT sent to: " + toEmail);
            return false;
        }
    }
}
