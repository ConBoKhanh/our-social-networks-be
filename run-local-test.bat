@echo off
echo Starting Our Social Networks Application (Local Test)
echo.

REM Set environment variables for local testing
set JWT_SECRET=mySecretKey123456789012345678901234567890
set JWT_EXPIRATION=3600000
set JWT_REFRESH_EXPIRATION=604800000

REM Supabase (use dummy values for compilation test)
set SUPABASE_URL=https://dummy.supabase.co
set SUPABASE_KEY=dummy-key

REM Google OAuth2 (use dummy values for compilation test)
set GOOGLE_CLIENT_ID=dummy-client-id
set GOOGLE_CLIENT_SECRET=dummy-client-secret
set GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

REM URLs
set FRONTEND_URL=http://localhost:3000
set BACKEND_URL=http://localhost:8080

REM Email Configuration - Gmail SMTP
set EMAIL_ENABLED=true
set EMAIL_HOST=smtp.gmail.com
set EMAIL_PORT=587
set EMAIL_USERNAME=your-gmail@gmail.com
set EMAIL_PASSWORD=your-app-password-16-chars
set EMAIL_SMTP_AUTH=true
set EMAIL_SSL_ENABLE=false
set EMAIL_STARTTLS=true
set EMAIL_FROM=your-gmail@gmail.com

REM Disable Resend (use Gmail instead)
set RESEND_ENABLED=false
set RESEND_API_KEY=
set RESEND_FROM_EMAIL=

REM OAuth2
set OAUTH2_FORCE_CONSENT=true

echo Environment variables set for local testing
echo.
echo Starting Spring Boot application...
echo.

./mvnw spring-boot:run

pause