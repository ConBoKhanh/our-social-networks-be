# OAuth2 Google Login Debug Guide

## Vấn đề đã được giải quyết

✅ **Vấn đề**: Google OAuth2 bị "treo" khi đã đăng nhập trước đó vì Google nhớ tài khoản và không hiển thị màn hình xin phép.

✅ **Giải pháp**: Thêm `prompt=consent` để buộc Google luôn hiển thị màn hình xin phép.

## Cách test

### 1. Kiểm tra cấu hình OAuth2
```bash
curl https://your-backend-url/api/debug/oauth2-config
```

### 2. Kiểm tra flow info
```bash
curl https://your-backend-url/api/debug/oauth2-flow-info
```

### 3. Test Google Login
1. Truy cập `/login`
2. Click "Đăng nhập bằng Google"
3. Google sẽ **luôn** hiển thị màn hình xin phép (không bị treo)
4. Sau khi cho phép, sẽ redirect về ứng dụng

## Cấu hình

### Environment Variables
```bash
# Buộc hiển thị màn hình consent (mặc định: true)
OAUTH2_FORCE_CONSENT=true

# Frontend URL
FRONTEND_URL=https://conbokhanh.io.vn

# Backend URL  
BACKEND_URL=https://your-backend-url
```

### Trong application.properties
```properties
# OAuth2 Settings
app.oauth2.force-consent=${OAUTH2_FORCE_CONSENT:true}
```

## Behavior

### Khi `OAUTH2_FORCE_CONSENT=true` (mặc định)
- Google **luôn** hiển thị màn hình xin phép
- Giải quyết vấn đề bị treo khi đã đăng nhập trước
- User sẽ thấy màn hình "Allow app to access your Google account"

### Khi `OAUTH2_FORCE_CONSENT=false`
- Google chỉ hiển thị màn hình chọn tài khoản
- Có thể bị treo nếu đã đăng nhập trước và đã cho phép

## Logs để theo dõi

Khi test, check logs để thấy:
```
✅ OAuth2: Force consent enabled - Google will show permission screen
=== OAuth2 Authorization Request Customized ===
Force consent: true
Original parameters: {}
Final parameters: {prompt=consent, access_type=offline}
===============================================
```

## Troubleshooting

### Nếu vẫn bị treo:
1. Kiểm tra `OAUTH2_FORCE_CONSENT=true` đã được set chưa
2. Kiểm tra redirect URI trong Google Console
3. Clear browser cache và cookies
4. Check server logs để thấy OAuth2 flow

### Debug endpoints:
- `/api/debug/oauth2-config` - Xem cấu hình OAuth2
- `/api/debug/oauth2-flow-info` - Xem thông tin flow
- `/api/debug/test-redirect` - Test basic redirect

## Kết quả mong đợi

✅ Google login không bị treo  
✅ Luôn hiển thị màn hình xin phép  
✅ Redirect về ứng dụng sau khi cho phép  
✅ Tạo user mới hoặc login user cũ thành công  