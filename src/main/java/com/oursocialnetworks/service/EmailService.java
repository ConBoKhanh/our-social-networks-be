package com.oursocialnetworks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
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
     * Gửi email thông báo tài khoản đã được kích hoạt với password mới
     */
    public void sendAccountActivatedEmail(String email, String username, String newPassword) {
        try {
            String subject = "Tài khoản Our Social Networks đã được kích hoạt";
            String body = String.format("""
                Xin chào %s,
                
                Tài khoản Our Social Networks của bạn đã được kích hoạt thành công thông qua Google OAuth2!
                
                Thông tin đăng nhập mới:
                - Email: %s
                - Username: %s
                - Mật khẩu mới: %s
                
                Bạn có thể:
                1. Đăng nhập bằng Google (khuyến nghị)
                2. Đăng nhập bằng username/password ở trên
                3. Đổi mật khẩu trong phần cài đặt tài khoản
                
                Chào mừng bạn đến với Our Social Networks!
                
                Trân trọng,
                Our Social Networks Team
                """, username, email, username, newPassword);
            
            sendEmail(email, subject, body);
            
        } catch (Exception e) {
            System.err.println("Failed to send account activated email to " + email + ": " + e.getMessage());
        }
    }

    private void sendEmail(String email, String subject, String body) {
        // Log thay vì gửi email thật (để test)
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("==========================");
        
        // TODO: Replace with actual email sending
        // mailSender.send(createMimeMessage(email, subject, body));
    }
}