package com.oursocialnetworks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;
import com.oursocialnetworks.service.EmailService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class OAuth2DebugController {

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.frontend.url:https://conbokhanh.io.vn}")
    private String frontendUrl;

    @Value("${app.backend.url:}")
    private String backendUrl;

    @GetMapping("/oauth2-config")
    public Map<String, Object> getOAuth2Config() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("frontendUrl", frontendUrl);
        config.put("backendUrl", backendUrl);
        
        if (clientRegistrationRepository == null) {
            config.put("error", "ClientRegistrationRepository is null");
            return config;
        }

        try {
            ClientRegistration googleReg = clientRegistrationRepository.findByRegistrationId("google");
            if (googleReg == null) {
                config.put("error", "Google ClientRegistration is null");
            } else {
                Map<String, Object> googleConfig = new HashMap<>();
                googleConfig.put("clientId", googleReg.getClientId() != null ? 
                    "***" + googleReg.getClientId().substring(Math.max(0, googleReg.getClientId().length() - 4)) : "NULL");
                googleConfig.put("clientSecretConfigured", googleReg.getClientSecret() != null);
                googleConfig.put("redirectUri", googleReg.getRedirectUri());
                googleConfig.put("authorizationUri", googleReg.getProviderDetails().getAuthorizationUri());
                googleConfig.put("tokenUri", googleReg.getProviderDetails().getTokenUri());
                googleConfig.put("scopes", googleReg.getScopes());
                config.put("google", googleConfig);
            }
        } catch (Exception e) {
            config.put("error", "Error getting Google registration: " + e.getMessage());
        }
        
        return config;
    }

    @Value("${app.oauth2.force-consent:true}")
    private boolean forceConsent;

    @GetMapping("/test-redirect")
    public Map<String, String> testRedirect() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "This endpoint works");
        result.put("frontendUrl", frontendUrl);
        result.put("forceConsent", String.valueOf(forceConsent));
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return result;
    }

    @Value("${spring.mail.host:}")
    private String mailHost;
    
    @Value("${spring.mail.port:587}")
    private String mailPort;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @GetMapping("/oauth2-flow-info")
    public Map<String, Object> getOAuth2FlowInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("forceConsent", forceConsent);
        info.put("description", forceConsent ? 
            "Google sẽ luôn hiển thị màn hình xin phép, giải quyết vấn đề khi Google nhớ tài khoản" :
            "Google chỉ hiển thị màn hình chọn tài khoản");
        info.put("solution", "Nếu bị treo ở Google login, hãy set OAUTH2_FORCE_CONSENT=true");
        return info;
    }

    @Autowired(required = false)
    private org.thymeleaf.TemplateEngine templateEngine;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired(required = false)
    private com.oursocialnetworks.service.ResendEmailService resendEmailService;

    @GetMapping("/email-config")
    public Map<String, Object> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("emailEnabled", emailEnabled);
        config.put("mailHost", mailHost);
        config.put("mailPort", mailPort);
        config.put("status", emailEnabled ? "enabled" : "disabled");
        config.put("templateEngineAvailable", templateEngine != null);
        
        if (!emailEnabled) {
            config.put("warning", "Email is disabled - temp passwords will only be logged");
        } else if (mailHost == null || mailHost.trim().isEmpty()) {
            config.put("error", "Mail host not configured");
        } else {
            config.put("info", "Email configured with " + mailHost + ":" + mailPort);
        }
        
        config.put("recommendations", Map.of(
            "port465", "Try SSL port 465 if 587 is blocked",
            "sendgrid", "Consider using SendGrid for better deliverability",
            "mailgun", "Consider using Mailgun as alternative",
            "awsSes", "Consider using AWS SES for production"
        ));
        
        return config;
    }

    @GetMapping("/email-preview/temp-password")
    public String previewTempPasswordEmail() {
        if (templateEngine == null) {
            return "<h1>Template Engine not available</h1>";
        }
        
        try {
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("username", "John Doe");
            context.setVariable("email", "john.doe@example.com");
            context.setVariable("tempPassword", "ABC123XYZ");
            context.setVariable("changePasswordUrl", "https://our-social-networks-be.onrender.com/change-password?email=john.doe@example.com");
            
            return templateEngine.process("email-temp-password", context);
        } catch (Exception e) {
            return "<h1>Error generating email preview: " + e.getMessage() + "</h1>";
        }
    }

    @GetMapping("/email-preview/new-account")
    public String previewNewAccountEmail() {
        if (templateEngine == null) {
            return "<h1>Template Engine not available</h1>";
        }
        
        try {
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("username", "Jane Smith");
            context.setVariable("email", "jane.smith@example.com");
            context.setVariable("tempPassword", "XYZ789ABC");
            
            return templateEngine.process("email-new-account", context);
        } catch (Exception e) {
            return "<h1>Error generating email preview: " + e.getMessage() + "</h1>";
        }
    }

    @PostMapping("/test-send-email")
    public ResponseEntity<?> testSendEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String username = request.getOrDefault("username", "Test User");
            String tempPassword = request.getOrDefault("tempPassword", "TEST123");
            
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (emailService == null) {
                return ResponseEntity.status(500).body(Map.of("error", "EmailService not available"));
            }
            
            System.out.println("🧪 [TEST] Sending email to: " + email);
            boolean sent = emailService.sendTempPasswordEmail(email, username, tempPassword);
            
            return ResponseEntity.ok(Map.of(
                "success", sent,
                "message", sent ? "Email sent successfully" : "Email failed to send",
                "email", email,
                "username", username,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to send email",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/resend-config")
    public Map<String, Object> getResendConfig() {
        Map<String, Object> config = new HashMap<>();
        
        if (resendEmailService == null) {
            config.put("error", "ResendEmailService not available");
            return config;
        }
        
        boolean isConfigured = resendEmailService.isConfigured();
        config.put("isConfigured", isConfigured);
        config.put("status", isConfigured ? "ready" : "not configured");
        
        return config;
    }

    @PostMapping("/force-send-email")
    public ResponseEntity<?> forceSendEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            System.out.println("🔥 [FORCE] Force sending email to: " + email);
            
            // Test cả 2 service
            Map<String, Object> result = new HashMap<>();
            
            // Test Resend
            if (resendEmailService != null) {
                boolean resendConfigured = resendEmailService.isConfigured();
                result.put("resendConfigured", resendConfigured);
                
                if (resendConfigured) {
                    boolean resendSent = resendEmailService.sendTempPasswordEmail(email, "Force Test", "FORCE123");
                    result.put("resendSent", resendSent);
                }
            }
            
            // Test EmailService
            if (emailService != null) {
                boolean emailSent = emailService.sendTempPasswordEmail(email, "Force Test", "FORCE123");
                result.put("emailServiceSent", emailSent);
            }
            
            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Force send failed",
                "message", e.getMessage()
            ));
        }
    }
}