package com.oursocialnetworks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    /**
     * Gửi email thông báo tài khoản mới được tạo
     */
    public void sendNewAccountEmail(String email, String username, String tempPassword) {
        try {
            // TODO: Implement actual email sending logic
            // Có thể dùng JavaMailSender, SendGrid, AWS SES, etc.
            
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
            
            // Log thay vì gửi email thật (để test)
            System.out.println("=== EMAIL NOTIFICATION ===");
            System.out.println("To: " + email);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("==========================");
            
            // TODO: Replace with actual email sending
            // mailSender.send(createMimeMessage(email, subject, body));
            
        } catch (Exception e) {
            System.err.println("Failed to send email to " + email + ": " + e.getMessage());
            // Don't throw exception - email failure shouldn't break user creation
        }
    }
}