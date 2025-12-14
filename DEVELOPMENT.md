# Development Guide

## Environment Configuration

### Local Development

1. **Run with local profile:**
   ```bash
   # Windows
   run-local.bat
   
   # Linux/Mac
   chmod +x run-local.sh
   ./run-local.sh
   
   # Or manually
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. **Local URLs:**
   - Backend: http://localhost:8080
   - Frontend: http://localhost:4200
   - Swagger UI: http://localhost:8080/swagger-ui.html

3. **Local Configuration:**
   - File: `application-local.properties`
   - CORS: Allows localhost:4200, localhost:3000
   - Logging: DEBUG level
   - JWT: 1 hour expiration

### Production Deployment

1. **Set environment variables:**
   ```bash
   export SPRING_PROFILES_ACTIVE=production
   export BACKEND_URL=https://your-backend.com
   export FRONTEND_URL=https://your-frontend.com
   export GOOGLE_CLIENT_ID=your-google-client-id
   export GOOGLE_CLIENT_SECRET=your-google-client-secret
   export JWT_SECRET=your-jwt-secret
   export SUPABASE_URL=your-supabase-url
   export SUPABASE_KEY=your-supabase-key
   ```

2. **Production Configuration:**
   - File: `application-production.properties`
   - CORS: Strict origins
   - Logging: INFO level
   - Swagger: Disabled by default

## OAuth2 Flow

### Local Development Flow:
1. Frontend: http://localhost:4200
2. Login button → http://localhost:8080/oauth2/authorization/google
3. Google OAuth2 → http://localhost:8080/login/oauth2/code/google
4. Backend processes → Redirect to http://localhost:4200/auth/callback?accessToken=...

### Production Flow:
1. Frontend: https://your-frontend.com
2. Login button → https://your-backend.com/oauth2/authorization/google
3. Google OAuth2 → https://your-backend.com/login/oauth2/code/google
4. Backend processes → Redirect to https://your-frontend.com/auth/callback?accessToken=...

## Configuration Files

- `application.properties` - Base configuration with defaults
- `application-local.properties` - Local development overrides
- `application-production.properties` - Production overrides

## Environment Variables

| Variable | Description | Local Default | Production Required |
|----------|-------------|---------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Active profile | local | production |
| `BACKEND_URL` | Backend URL | http://localhost:8080 | Yes |
| `FRONTEND_URL` | Frontend URL | http://localhost:4200 | Yes |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | From properties | Yes |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Secret | From properties | Yes |
| `JWT_SECRET` | JWT signing secret | From properties | Yes |
| `SUPABASE_URL` | Supabase URL | From properties | Yes |
| `SUPABASE_KEY` | Supabase API Key | From properties | Yes |

## Security Notes

- Never commit `application-local.properties` to production
- Always use environment variables in production
- JWT secrets should be strong and unique per environment
- CORS origins should be restrictive in production