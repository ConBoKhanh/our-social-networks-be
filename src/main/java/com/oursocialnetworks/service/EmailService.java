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

    @Value("${app.backend.url:https://our-social-networks-be.onrender.com}")
    private String backendUrl;

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
     * Ưu tiên sử dụng Gmail SMTP, fallback sang Resend API nếu Gmail không available
     */
    public boolean sendTempPasswordEmail(String email, String username, String tempPassword) {
        try {
            System.out.println("📧 Sending temp password email to: " + email);
            
            // Ưu tiên sử dụng Resend API (works on Render)
            if (resendEmailService.isConfigured()) {
                System.out.println("📧 Using Resend API (Render compatible)...");
                boolean result = resendEmailService.sendTempPasswordEmail(email, username, tempPassword);
                if (result) {
                    System.out.println("✅ Email sent via Resend API");
                    return true;
                }
                System.out.println("⚠️ Resend failed, trying Gmail SMTP fallback...");
            }
            
            // Fallback sang Gmail SMTP (chỉ hoạt động local, Render sẽ block)
            if (emailEnabled && emailUsername != null && !emailUsername.trim().isEmpty()) {
                System.out.println("📧 Using Gmail SMTP (may fail on Render)...");
                
                String subject = "🔐 Mật khẩu tạm thời cho tài khoản ConBoKhanh của bạn";
                
                Context context = new Context();
                context.setVariable("username", username);
                context.setVariable("email", email);
                context.setVariable("tempPassword", tempPassword);
                context.setVariable("changePasswordUrl", backendUrl + "/change-password?email=" + email);
                
                String htmlContent = templateEngine.process("email-temp-password", context);
                
                boolean result = sendHtmlEmailInternal(email, subject, htmlContent);
                
                if (result) {
                    System.out.println("✅ Email sent via Gmail SMTP");
                    return true;
                }
                System.out.println("❌ Gmail SMTP failed (expected on Render)");
            }
            
            System.err.println("❌ No email service available or all failed");
            return false;
            
        } catch (Exception e) {
            System.err.println("📧 Failed to send temp password email to " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Internal method để gửi HTML email qua Gmail SMTP
     */
    private boolean sendHtmlEmailInternal(String toEmail, String subject, String htmlContent) {
        try {
            // Kiểm tra nếu email bị tắt hoặc chưa config
            if (!emailEnabled || emailUsername == null || emailUsername.trim().isEmpty()) {
                System.out.println("=== EMAIL DISABLED - SKIPPING ===");
                return false;
            }

            System.out.println("📧 [Gmail SMTP] Attempting to send email to: " + toEmail);
            System.out.println("📧 [Gmail SMTP] From: " + fromEmail);
            System.out.println("📧 [Gmail SMTP] Username: " + emailUsername);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Thêm headers để tránh spam
            mimeMessage.setHeader("X-Mailer", "ConBoKhanh Social Network");
            mimeMessage.setHeader("X-Priority", "3");
            
            mailSender.send(mimeMessage);
            
            System.out.println("✅ [Gmail SMTP] Email sent successfully to: " + toEmail);
            return true;
            
        } catch (org.springframework.mail.MailAuthenticationException e) {
            System.err.println("❌ [Gmail SMTP] Authentication failed: " + e.getMessage());
            System.err.println("💡 Kiểm tra lại Gmail và App Password");
            System.err.println("💡 Đảm bảo đã bật 2-Step Verification và tạo App Password");
            return false;
        } catch (org.springframework.mail.MailSendException e) {
            System.err.println("❌ [Gmail SMTP] Send failed: " + e.getMessage());
            System.err.println("💡 Kiểm tra kết nối internet và cấu hình SMTP");
            return false;
        } catch (Exception e) {
            System.err.println("❌ [Gmail SMTP] Unexpected error: " + e.getMessage());
            System.err.println("💡 Chi tiết lỗi: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }
}
