#!/bin/bash

# Test script for change-password-new-user endpoint
# Usage: ./test-change-password.sh

API_URL="https://our-social-networks-be.onrender.com"
# API_URL="http://localhost:8080"  # For local testing

echo "🧪 Testing change-password-new-user endpoint..."
echo "API URL: $API_URL"

# Test data
EMAIL="duybb69@gmail.com"
TEMP_PASSWORD="TzKA55ia"
NEW_PASSWORD="Duytunbua@2003"
CONFIRM_PASSWORD="Duytunbua@2003"

echo ""
echo "📋 Test data:"
echo "Email: $EMAIL"
echo "Temp Password: $TEMP_PASSWORD"
echo "New Password: $NEW_PASSWORD"

echo ""
echo "🚀 Sending request..."

curl -X POST "$API_URL/auth/change-password-new-user" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"tempPassword\": \"$TEMP_PASSWORD\",
    \"newPassword\": \"$NEW_PASSWORD\",
    \"confirmPassword\": \"$CONFIRM_PASSWORD\"
  }" \
  -w "\n\n📊 Response Info:\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\nResponse Size: %{size_download} bytes\n" \
  -v

echo ""
echo "✅ Test completed!"
echo ""
echo "💡 Expected results:"
echo "- HTTP 200: Success"
echo "- HTTP 400: Bad request (wrong temp password, validation error)"
echo "- HTTP 401: Unauthorized (wrong email/temp password)"
echo "- HTTP 500: Server error (database issue)"