# 📧 Beautiful Email Templates Guide

## ✨ Đã tạo email templates đẹp!

### 🎨 **Email Templates mới**
1. **`email-temp-password.html`** - Email mật khẩu tạm thời (cho OAuth2 users)
2. **`email-new-account.html`** - Email tài khoản mới (cho regular signup)

### 🌟 **Tính năng**
- ✅ **Responsive design** - Hoạt động tốt trên mobile và desktop
- ✅ **Instagram-style branding** - Logo "conbokhanh" với font Billabong
- ✅ **Beautiful gradients** - Purple gradient header
- ✅ **Clear password display** - Monospace font cho mật khẩu
- ✅ **Step-by-step instructions** - Hướng dẫn rõ ràng
- ✅ **Warning alerts** - Cảnh báo bảo mật
- ✅ **CTA buttons** - Nút "Đổi mật khẩu ngay"
- ✅ **Professional footer** - Thông tin liên hệ

## 🔧 **Technical Implementation**

### EmailService đã được cập nhật:
- Sử dụng `MimeMessageHelper` cho HTML emails
- Template engine integration với Thymeleaf
- Retry logic với better error handling
- HTML content logging khi email fail

### Template Variables:
```java
// For temp-password email
context.setVariable("username", username);
context.setVariable("email", email);
context.setVariable("tempPassword", tempPassword);
context.setVariable("changePasswordUrl", "https://conbokhanh.io.vn/change-password?email=" + email);

// For new-account email  
context.setVariable("username", username);
context.setVariable("email", email);
context.setVariable("tempPassword", tempPassword);
```

## 🎯 **Preview Endpoints**

### Test email templates:
```bash
# Preview temp password email
curl https://your-backend-url/api/debug/email-preview/temp-password

# Preview new account email  
curl https://your-backend-url/api/debug/email-preview/new-account

# Check email configuration
curl https://your-backend-url/api/debug/email-config
```

## 📱 **Email Design Features**

### 🎨 **Visual Elements**
- **Header**: Purple gradient với logo "conbokhanh"
- **Password Box**: Highlighted với monospace font
- **Steps**: Numbered steps với icons
- **Warning**: Red border alert box
- **CTA Button**: Gradient button "Đổi mật khẩu ngay"
- **Footer**: Dark footer với social links

### 📐 **Responsive Design**
```css
@media (max-width: 600px) {
    .email-container { border-radius: 0; }
    .logo { font-size: 36px; }
    .password-value { font-size: 24px; }
}
```

## 🚀 **Usage**

### Temp Password Email (OAuth2):
```java
emailService.sendTempPasswordEmail(email, username, tempPassword);
```

### New Account Email (Regular signup):
```java
emailService.sendNewAccountEmail(email, username, tempPassword);
```

## 🎨 **Email Content**

### **Temp Password Email includes:**
- 👋 Personal greeting
- 🔐 Highlighted temp password
- ⚠️ Security warning
- 📋 4-step instructions
- 🔗 Direct link to change password
- ✅ Benefits after password change

### **New Account Email includes:**
- 🎉 Welcome message
- 📊 Account info table
- 🔐 Temp password display
- ⚠️ Security reminder

## 🌈 **Brand Consistency**
- **Colors**: Purple gradient (#667eea to #764ba2)
- **Font**: Billabong for logo, system fonts for content
- **Style**: Instagram-inspired dark theme
- **Icons**: Emoji icons for better readability

## 🔍 **Testing**
1. Visit `/api/debug/email-preview/temp-password` để xem preview
2. Visit `/api/debug/email-preview/new-account` để xem preview  
3. Check logs khi gửi email thật để debug

## 📧 **Email Subject Lines**
- Temp password: `🔐 Mật khẩu tạm thời - conbokhanh`
- New account: `🎉 Tài khoản conbokhanh của bạn đã được tạo`

Emails bây giờ sẽ trông professional và đẹp mắt! 🎨✨