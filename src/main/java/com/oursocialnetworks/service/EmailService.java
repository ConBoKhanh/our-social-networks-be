package com.oursocialnetworks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
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
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:}")
    private String emailUsername;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    /**
     * Gửi email thông báo tài khoản mới được tạo với password tạm thời
     */
    public void sendNewAccountEmail(String email, String username, String tempPassword) {
        try {
            String subject = "🎉 Tài khoản conbokhanh của bạn đã được tạo";
            
            // Tạo HTML content từ template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("email", email);
            context.setVariable("tempPassword", tempPassword);
            
            String htmlContent = templateEngine.process("email-new-account", context);
            
            sendHtmlEmail(email, subject, htmlContent);
            
        } catch (Exception e) {
            System.err.println("Failed to send new account email to " + email + ": " + e.getMessage());
        }
    }

    /**
     * Gửi email mật khẩu tạm thời cho user mới từ Google OAuth2
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public boolean sendTempPasswordEmail(String email, String username, String tempPassword) {
        try {
            String subject = "🔐 Mật khẩu tạm thời - conbokhanh";
            
            // Tạo HTML content từ template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("email", email);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("changePasswordUrl", "https://conbokhanh.io.vn/change-password?email=" + email);
            
            String htmlContent = templateEngine.process("email-temp-password", context);
            
            return sendHtmlEmail(email, subject, htmlContent);
            
        } catch (Exception e) {
            System.err.println("Failed to send temp password email to " + email + ": " + e.getMessage());
            return false;
        }
    }

    private boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            // Kiểm tra nếu email bị tắt hoặc chưa config
            if (!emailEnabled || emailUsername == null || emailUsername.trim().isEmpty()) {
                System.out.println("=== EMAIL DISABLED/NOT CONFIGURED - LOGGING ONLY ===");
                System.out.println("To: " + toEmail);
                System.out.println("Subject: " + subject);
                System.out.println("HTML Content: " + htmlContent.substring(0, Math.min(500, htmlContent.length())) + "...");
                System.out.println("Email enabled: " + emailEnabled);
                System.out.println("Email username configured: " + (emailUsername != null && !emailUsername.trim().isEmpty()));
                System.out.println("====================================================");
                return true; // Trả về true vì đã "gửi" (log)
            }

            // Retry logic với timeout ngắn hơn
            int maxRetries = 2;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    System.out.println("📧 Attempting to send HTML email (attempt " + attempt + "/" + maxRetries + ")...");
                    
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    
                    helper.setFrom(fromEmail);
                    helper.setTo(toEmail);
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true); // true = HTML content
                    
                    mailSender.send(mimeMessage);
                    
                    System.out.println("✅ HTML Email sent successfully to: " + toEmail + " (attempt " + attempt + ")");
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("❌ HTML Email attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (attempt == maxRetries) {
                        // Log chi tiết lỗi cuối cùng
                        System.err.println("=== FINAL HTML EMAIL ERROR DETAILS ===");
                        System.err.println("Error type: " + e.getClass().getSimpleName());
                        System.err.println("Error message: " + e.getMessage());
                        if (e.getCause() != null) {
                            System.err.println("Root cause: " + e.getCause().getMessage());
                        }
                        System.err.println("Possible solutions:");
                        System.err.println("1. Check if SMTP port 465 (SSL) or 587 (TLS) is blocked by hosting provider");
                        System.err.println("2. Try using SendGrid, Mailgun, or AWS SES instead of Gmail SMTP");
                        System.err.println("3. Check Gmail App Password is correct");
                        System.err.println("=====================================");
                        
                        // Log email content for debugging
                        System.out.println("=== HTML EMAIL FAILED - CONTENT ===");
                        System.out.println("To: " + toEmail);
                        System.out.println("Subject: " + subject);
                        System.out.println("HTML Content: " + htmlContent.substring(0, Math.min(1000, htmlContent.length())) + "...");
                        System.out.println("===================================");
                        return false;
                    } else {
                        // Wait before retry
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in sendHtmlEmail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        try {
            // Kiểm tra nếu email bị tắt hoặc chưa config
            if (!emailEnabled || emailUsername == null || emailUsername.trim().isEmpty()) {
                System.out.println("=== EMAIL DISABLED/NOT CONFIGURED - LOGGING ONLY ===");
                System.out.println("To: " + toEmail);
                System.out.println("Subject: " + subject);
                System.out.println("Body: " + body);
                System.out.println("Email enabled: " + emailEnabled);
                System.out.println("Email username configured: " + (emailUsername != null && !emailUsername.trim().isEmpty()));
                System.out.println("====================================================");
                return true; // Trả về true vì đã "gửi" (log)
            }

            // Retry logic với timeout ngắn hơn
            int maxRetries = 2;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    System.out.println("📧 Attempting to send email (attempt " + attempt + "/" + maxRetries + ")...");
                    
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(toEmail);
                    message.setSubject(subject);
                    message.setText(body);

                    mailSender.send(message);
                    
                    System.out.println("✅ Email sent successfully to: " + toEmail + " (attempt " + attempt + ")");
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("❌ Email attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (attempt == maxRetries) {
                        // Log chi tiết lỗi cuối cùng
                        System.err.println("=== FINAL EMAIL ERROR DETAILS ===");
                        System.err.println("Error type: " + e.getClass().getSimpleName());
                        System.err.println("Error message: " + e.getMessage());
                        if (e.getCause() != null) {
                            System.err.println("Root cause: " + e.getCause().getMessage());
                        }
                        System.err.println("Possible solutions:");
                        System.err.println("1. Check if SMTP port 465 (SSL) or 587 (TLS) is blocked by hosting provider");
                        System.err.println("2. Try using SendGrid, Mailgun, or AWS SES instead of Gmail SMTP");
                        System.err.println("3. Check Gmail App Password is correct");
                        System.err.println("================================");
                        
                        // Log email content for debugging
                        System.out.println("=== EMAIL FAILED - CONTENT ===");
                        System.out.println("To: " + toEmail);
                        System.out.println("Subject: " + subject);
                        System.out.println("Body: " + body);
                        System.out.println("==============================");
                        return false;
                    } else {
                        // Wait before retry
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in sendEmail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}