# Email Deliverability Guide - Tránh Email Vào Spam

## Vấn đề hiện tại
1. **Database Error**: `"column pgrst_body.id does not exist"` khi đổi mật khẩu
2. **Email vào spam**: Email gửi từ `onboarding@resend.dev` bị Gmail đánh dấu spam

## Giải pháp đã áp dụng

### 1. Fix Database Error
- **Thay đổi**: Sử dụng `PATCH` thay vì `PUT` trong `AuthController.changePasswordNewUser()`
- **Lý do**: PostgREST có thể xử lý PATCH tốt hơn PUT cho một số trường hợp
- **File đã sửa**: `src/main/java/com/oursocialnetworks/controller/AuthController.java`

### 2. Cải thiện Email Deliverability

#### A. Thay đổi sender domain
```java
// Trước (dễ bị spam):
body.put("from", "onboarding@resend.dev");

// Sau (tăng độ tin cậy):
body.put("from", "ConBoKhanh <noreply@conbokhanh.io.vn>");
body.put("reply_to", "support@conbokhanh.io.vn");
```

#### B. Cải thiện nội dung email
- Giảm từ ngữ "khẩn cấp" (QUAN TRỌNG, ngay lập tức)
- Thêm thông tin về thời hạn mật khẩu (24 giờ)
- Sử dụng ngôn ngữ chuyên nghiệp hơn

## Cấu hình cần thiết để tránh spam hoàn toàn

### 1. Domain Authentication (Quan trọng nhất)

#### A. Cấu hình DNS Records cho conbokhanh.io.vn

```dns
# SPF Record
TXT @ "v=spf1 include:_spf.resend.com ~all"

# DKIM Record (lấy từ Resend Dashboard)
TXT resend._domainkey "p=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC..."

# DMARC Record
TXT _dmarc "v=DMARC1; p=quarantine; rua=mailto:dmarc@conbokhanh.io.vn"
```

#### B. Verify Domain trong Resend
1. Đăng nhập https://resend.com/domains
2. Add domain: `conbokhanh.io.vn`
3. Thêm DNS records theo hướng dẫn
4. Verify domain

### 2. Environment Variables cần cập nhật

```bash
# Resend Configuration
RESEND_API_KEY=re_xxxxxxxxxx
RESEND_FROM_EMAIL=noreply@conbokhanh.io.vn
RESEND_ENABLED=true

# Disable SMTP (vì Render block)
EMAIL_ENABLED=false
```

### 3. Cải thiện nội dung email thêm

#### A. Thêm unsubscribe link (bắt buộc)
```html
<div class="footer-text">
    <a href="mailto:support@conbokhanh.io.vn?subject=Unsubscribe">Hủy đăng ký nhận email</a>
</div>
```

#### B. Thêm physical address (khuyến nghị)
```html
<div class="footer-text">
    ConBoKhanh Social Network<br>
    Địa chỉ: [Địa chỉ công ty]<br>
    Email: support@conbokhanh.io.vn
</div>
```

### 4. Monitoring và Testing

#### A. Test email deliverability
```bash
# Sử dụng mail-tester.com
curl -X POST https://api.resend.com/emails \
  -H "Authorization: Bearer $RESEND_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "ConBoKhanh <noreply@conbokhanh.io.vn>",
    "to": "test-xxxxx@mail-tester.com",
    "subject": "Test Email Deliverability",
    "html": "<p>Testing email deliverability</p>"
  }'
```

#### B. Monitor bounce rates
- Theo dõi bounce rate trong Resend Dashboard
- Nếu > 5% cần review lại content
- Nếu > 10% có thể bị blacklist

## Checklist triển khai

### Ngay lập tức (đã làm)
- [x] Fix database error với PATCH
- [x] Thay đổi sender domain
- [x] Cải thiện email content

### Trong 24h
- [ ] Cấu hình DNS records cho conbokhanh.io.vn
- [ ] Verify domain trong Resend
- [ ] Update environment variables
- [ ] Test với mail-tester.com

### Trong tuần
- [ ] Thêm unsubscribe link
- [ ] Thêm company address
- [ ] Monitor deliverability metrics
- [ ] Setup email analytics

## Kết quả mong đợi

Sau khi áp dụng đầy đủ:
- **Deliverability**: 95%+ vào inbox
- **Spam score**: < 3/10
- **Domain reputation**: Tốt
- **User experience**: Email đến inbox nhanh chóng

## Lưu ý quan trọng

1. **Domain verification là quan trọng nhất** - không có domain riêng sẽ luôn có risk spam
2. **Content quality** - tránh từ ngữ spam trigger
3. **Sending volume** - bắt đầu với volume thấp, tăng dần
4. **List hygiene** - remove bounce/invalid emails
5. **Engagement** - khuyến khích user tương tác với email

## Troubleshooting

### Nếu vẫn vào spam sau khi cấu hình
1. Check DNS propagation: `dig TXT conbokhanh.io.vn`
2. Verify DKIM signature: mail-tester.com
3. Check domain reputation: sender-score.org
4. Review email content với spam checker tools

### Nếu database error vẫn xảy ra
1. Check Supabase logs
2. Verify table structure
3. Test với Postman/curl trực tiếp
4. Check RLS policies