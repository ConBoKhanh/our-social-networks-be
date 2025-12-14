package com.oursocialnetworks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ConfigDebug {

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void debugOAuth2Config() {
        System.out.println("========== OAUTH2 CONFIG DEBUG ==========");
        
        if (clientRegistrationRepository == null) {
            System.out.println("❌ ClientRegistrationRepository is NULL");
            return;
        }

        try {
            ClientRegistration googleReg = clientRegistrationRepository.findByRegistrationId("google");
            if (googleReg == null) {
                System.out.println("❌ Google ClientRegistration is NULL");
            } else {
                System.out.println("✅ Google OAuth2 Configuration:");
                System.out.println("Client ID: " + (googleReg.getClientId() != null ? "***" + googleReg.getClientId().substring(Math.max(0, googleReg.getClientId().length() - 4)) : "NULL"));
                System.out.println("Client Secret: " + (googleReg.getClientSecret() != null ? "***configured" : "NULL"));
                System.out.println("Redirect URI: " + googleReg.getRedirectUri());
                System.out.println("Authorization URI: " + googleReg.getProviderDetails().getAuthorizationUri());
                System.out.println("Token URI: " + googleReg.getProviderDetails().getTokenUri());
                System.out.println("Scopes: " + googleReg.getScopes());
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting Google registration: " + e.getMessage());
        }
        
        System.out.println("=========================================");
    }
}