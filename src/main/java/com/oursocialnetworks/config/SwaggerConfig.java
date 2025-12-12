package com.oursocialnetworks.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // ✅ Tạo Security Scheme cho JWT Bearer Token
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token in the format: Bearer {token}");

        // ✅ Tạo Security Requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Our Social Networks API")
                        .version("1.0.0")
                        .description("""
                                API Documentation for Our Social Networks Application
                                
                                ## Authentication
                                Most endpoints require JWT authentication. To test protected endpoints:
                                1. Login via OAuth2: Navigate to `/oauth2/authorization/google`
                                2. After login, you'll receive an access token
                                3. Click the 🔓 Authorize button above
                                4. Enter: `Bearer YOUR_ACCESS_TOKEN`
                                5. Click Authorize and test APIs
                                
                                ## Public Endpoints (No Auth Required)
                                - `POST /auth/login` - Login with Google ID token
                                - `GET /api/health` - Health check
                                - `GET /api/ping` - Simple ping
                                - `GET /api/info` - Server info
                                
                                ## Protected Endpoints (Require JWT)
                                - All `/api/users/**` endpoints
                                - `GET /auth/check` - Check login status
                                - `POST /auth/refresh` - Refresh access token
                                """)
                        .contact(new Contact()
                                .name("Development Team")
                                .email("support@oursocialnetworks.com")
                                .url("https://oursocialnetworks.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://our-social-networks-be.onrender.com")
                                .description("Production Server")
                ))
                // ✅ Thêm Security Scheme vào Components
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                // ✅ Apply security requirement globally
                .addSecurityItem(securityRequirement);
    }
}