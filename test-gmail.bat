@echo off
echo ========================================
echo    TEST GMAIL EMAIL SENDING
echo ========================================
echo.

set /p GMAIL_ADDRESS="Nhập Gmail của bạn: "
set /p APP_PASSWORD="Nhập App Password: "
set /p TEST_EMAIL="Nhập email để test gửi đến: "

echo.
echo Testing Gmail configuration...
echo Gmail: %GMAIL_ADDRESS%
echo Test Email: %TEST_EMAIL%
echo.

curl -X POST "http://localhost:8080/oauth2/debug/test-email" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"%TEST_EMAIL%\", \"username\": \"Test User\", \"tempPassword\": \"TEST123\"}" ^
  -w "\nHTTP Status: %%{http_code}\nTotal Time: %%{time_total}s\n"

echo.
echo Kiểm tra email trong hộp thư của %TEST_EMAIL%
pause