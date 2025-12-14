package com.oursocialnetworks.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;
    
    @Value("${app.oauth2.force-consent:true}")
    private boolean forceConsent;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultAuthorizationRequestResolver = 
            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = 
            this.defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        
        if (forceConsent) {
            // Thêm prompt=consent để buộc Google hiển thị lại màn hình xin phép
            // Điều này giải quyết vấn đề khi Google đã nhớ tài khoản từ lần đăng nhập trước
            additionalParameters.put("prompt", "consent");
            System.out.println("✅ OAuth2: Force consent enabled - Google will show permission screen");
        } else {
            // Chỉ hiển thị màn hình chọn tài khoản
            additionalParameters.put("prompt", "select_account");
            System.out.println("✅ OAuth2: Select account only - Google will show account selection");
        }
        
        // Thêm access_type=offline để có thể lấy refresh token (tùy chọn)
        additionalParameters.put("access_type", "offline");
        
        System.out.println("=== OAuth2 Authorization Request Customized ===");
        System.out.println("Force consent: " + forceConsent);
        System.out.println("Original parameters: " + authorizationRequest.getAdditionalParameters());
        System.out.println("Final parameters: " + additionalParameters);
        System.out.println("===============================================");
        
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}