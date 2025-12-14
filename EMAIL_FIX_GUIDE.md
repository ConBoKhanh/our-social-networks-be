# Email Service Fix Guide

## ✅ OAuth2 đã hoạt động!
User mới đã được tạo thành công, chỉ còn vấn đề gửi email.

## ❌ Vấn đề hiện tại
```
Mail server connection failed. Failed messages: 
org.eclipse.angus.mail.util.MailConnectException: 
Couldn't connect to host, port: smtp.gmail.com, 587; timeout -1
```

## 🔍 Nguyên nhân
Render.com (và nhiều hosting khác) **block port 587** để tránh spam.

## 🛠️ Giải pháp

### Option 1: Sử dụng Port 465 (SSL) thay vì 587 (TLS)
```bash
# Environment variables
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=465
EMAIL_USERNAME=duybb69@gmail.com
EMAIL_PASSWORD=quid gujc yfys wdda
EMAIL_SSL_ENABLE=true
EMAIL_SSL_REQUIRED=true
EMAIL_STARTTLS=false
```

### Option 2: Sử dụng SendGrid (Khuyến nghị)
```bash
# SendGrid SMTP
EMAIL_HOST=smtp.sendgrid.net
EMAIL_PORT=587
EMAIL_USERNAME=apikey
EMAIL_PASSWORD=your_sendgrid_api_key
EMAIL_STARTTLS=true
```

### Option 3: Sử dụng Mailgun
```bash
# Mailgun SMTP
EMAIL_HOST=smtp.mailgun.org
EMAIL_PORT=587
EMAIL_USERNAME=postmaster@your-domain.mailgun.org
EMAIL_PASSWORD=your_mailgun_password
EMAIL_STARTTLS=true
```

### Option 4: Disable Email (Temporary)
```bash
EMAIL_ENABLED=false
```
User vẫn có thể đổi mật khẩu bằng temp password được log trong console.

## 🧪 Test Email Config

### 1. Kiểm tra cấu hình
```bash
curl https://your-backend-url/api/debug/email-config
```

### 2. Kiểm tra logs
Temp password sẽ được log trong console:
```
=== EMAIL FAILED - CONTENT ===
To: nguyenkhanhduy7102003@gmail.com
Subject: Mật khẩu tạm thời - Our Social Networks
Body: [temp password content]
==============================
```

## 📋 Current Temp Password
Từ log hiện tại, user có thể sử dụng temp password để đổi mật khẩu.

## 🎯 Khuyến nghị
1. **Ngay lập tức**: Set `EMAIL_PORT=465` và `EMAIL_SSL_ENABLE=true`
2. **Dài hạn**: Chuyển sang SendGrid hoặc AWS SES cho production
3. **Backup**: Luôn log temp password để user có thể sử dụng khi email fail

## 🔧 Environment Variables cần set
```bash
# Try port 465 first
EMAIL_PORT=465
EMAIL_SSL_ENABLE=true
EMAIL_SSL_REQUIRED=true
EMAIL_STARTTLS=false

# Or use SendGrid
EMAIL_HOST=smtp.sendgrid.net
EMAIL_PORT=587
EMAIL_USERNAME=apikey
EMAIL_PASSWORD=your_sendgrid_api_key
```