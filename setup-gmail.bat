@echo off
echo ========================================
echo    SETUP GMAIL FOR EMAIL SENDING
echo ========================================
echo.

echo BƯỚC 1: Tạo App Password cho Gmail
echo 1. Đăng nhập Gmail → Quản lý tài khoản Google
echo 2. Bảo mật → Xác minh 2 bước (phải bật trước)
echo 3. Mật khẩu ứng dụng → Tạo mật khẩu mới
echo 4. Chọn "Mail" → Tạo
echo 5. Sao chép mật khẩu 16 ký tự
echo.

set /p GMAIL_ADDRESS="Nhập Gmail của bạn (ví dụ: duybb69@gmail.com): "
set /p APP_PASSWORD="Nhập App Password 16 ký tự (ví dụ: abcd efgh ijkl mnop): "

echo.
echo ========================================
echo    TESTING GMAIL CONFIGURATION
echo ========================================

REM Set Gmail configuration
set EMAIL_ENABLED=true
set EMAIL_HOST=smtp.gmail.com
set EMAIL_PORT=587
set EMAIL_USERNAME=%GMAIL_ADDRESS%
set EMAIL_PASSWORD=%APP_PASSWORD%
set EMAIL_SMTP_AUTH=true
set EMAIL_SSL_ENABLE=false
set EMAIL_STARTTLS=true
set EMAIL_FROM=%GMAIL_ADDRESS%

REM Disable Resend
set RESEND_ENABLED=false

REM Other required variables
set JWT_SECRET=mySecretKey123456789012345678901234567890
set SUPABASE_URL=https://dummy.supabase.co
set SUPABASE_KEY=dummy-key
set GOOGLE_CLIENT_ID=dummy-client-id
set GOOGLE_CLIENT_SECRET=dummy-client-secret
set GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
set FRONTEND_URL=http://localhost:3000
set BACKEND_URL=http://localhost:8080
set OAUTH2_FORCE_CONSENT=true

echo Gmail: %GMAIL_ADDRESS%
echo App Password: %APP_PASSWORD%
echo.
echo Starting application with Gmail configuration...
echo.

./mvnw spring-boot:run

pause