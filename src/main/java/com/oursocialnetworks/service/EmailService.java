package com.oursocialnetworks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:}")
    private String emailUsername;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Gửi email thông báo tài khoản mới được tạo với password tạm thời
     */
    public void sendNewAccountEmail(String email, String username, String tempPassword) {
        try {
            String subject = "Tài khoản Our Social Networks của bạn đã được tạo";
            String body = String.format("""
                Xin chào,
                
                Tài khoản Our Social Networks của bạn đã được tạo thành công!
                
                Thông tin đăng nhập:
                - Email: %s
                - Username: %s
                - Mật khẩu tạm thời: %s
                
                Vui lòng đăng nhập và đổi mật khẩu ngay lập tức để bảo mật tài khoản.
                
                Trân trọng,
                Our Social Networks Team
                """, email, username, tempPassword);
            
            sendEmail(email, subject, body);
            
        } catch (Exception e) {
            System.err.println("Failed to send new account email to " + email + ": " + e.getMessage());
        }
    }

    /**
     * Gửi email mật khẩu tạm thời cho user mới từ Google OAuth2
     */
    public void sendTempPasswordEmail(String email, String username, String tempPassword) {
        try {
            String subject = "Mật khẩu tạm thời - Our Social Networks";
            String body = String.format("""
                Xin chào %s,
                
                Tài khoản Our Social Networks của bạn đã được tạo thành công thông qua Google OAuth2!
                
                Để hoàn tất quá trình đăng ký, vui lòng sử dụng mật khẩu tạm thời dưới đây để đổi sang mật khẩu mới:
                
                Thông tin đăng nhập tạm thời:
                - Email: %s
                - Username: %s
                - Mật khẩu tạm thời: %s
                
                ⚠️ QUAN TRỌNG: Vui lòng đổi mật khẩu ngay lập tức để bảo mật tài khoản.
                
                Sau khi đổi mật khẩu, bạn có thể:
                1. Đăng nhập bằng Google (khuyến nghị)
                2. Đăng nhập bằng username/password mới
                
                Chào mừng bạn đến với Our Social Networks!
                
                Trân trọng,
                Our Social Networks Team
                """, username, email, username, tempPassword);
            
            sendEmail(email, subject, body);
            
        } catch (Exception e) {
            System.err.println("Failed to send temp password email to " + email + ": " + e.getMessage());
        }
    }

    private void sendEmail(String toEmail, String subject, String body) {
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
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            
            System.out.println("✅ Email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            
            // Log email content for debugging
            System.out.println("=== EMAIL FAILED - CONTENT ===");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("==============================");
        }
    }
}