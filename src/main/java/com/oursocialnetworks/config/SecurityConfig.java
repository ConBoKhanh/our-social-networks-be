package com.oursocialnetworks.config;

import com.oursocialnetworks.component.JwtAuthFilter;
import com.oursocialnetworks.component.OAuth2SuccessHandler;
import com.oursocialnetworks.component.CustomAuthenticationEntryPoint;
import com.oursocialnetworks.component.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http))

                // Stateless session cho JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // ✅ Swagger UI - Public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api-docs/**"
                        ).permitAll()

                        // ✅ Login Page & OAuth2 - Public
                        .requestMatchers(
                                "/login",                    // Trang login HTML
                                "/oauth2/**",                // OAuth2 endpoints
                                "/login/oauth2/**",          // OAuth2 callback
                                "/auth/login",               // JWT login endpoint
                                "/auth/login/basic",         // Username/password login endpoint
                                "/auth/callback",            // Auth callback endpoint
                                "/auth/oauth2/**"            // Auth OAuth2 endpoints
                        ).permitAll()

                        // ✅ Health Check - Public
                        .requestMatchers(
                                "/api/health",
                                "/api/ping",
                                "/api/info",
                                "/api/debug/public-test",
                                "/api/debug/test-patch/**",
                                "/api/users/test-delete/**"
                        ).permitAll()

                        // ⚠️ Protected Auth Endpoints - Require JWT
                        .requestMatchers(
                                "/auth/check",
                                "/auth/refresh",
                                "/auth/logout"
                        ).authenticated()

                        // ✅ Test role endpoints
                        .requestMatchers("/api/test-role/info").authenticated()
                        .requestMatchers("/api/test-role/user-only").hasRole("USER")
                        .requestMatchers("/api/test-role/admin-only").hasRole("ADMIN")
                        
                        // ✅ Client API - Authenticated user (tạm thời không cần role)
                        .requestMatchers("/api/client/**").authenticated()
                        
                        // ⚠️ Admin API - Admin role  
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        
                        // ⚠️ TẤT CẢ API KHÁC - Cần JWT
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                // ✅ Thêm JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ Custom Exception Handlers - Trả JSON thay vì HTML
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)  // 401 Unauthorized
                        .accessDeniedHandler(customAccessDeniedHandler)            // 403 Forbidden
                );

        // ✅ Chỉ cấu hình OAuth2 nếu có đăng ký client
        if (clientRegistrationRepository != null) {
            ClientRegistration first = clientRegistrationRepository.findByRegistrationId("google");
            if (first != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")                           // Custom login page
                    .successHandler(oAuth2SuccessHandler)          // Handler callback
                    .permitAll()
            );
        }
        }

        return http.build();
    }
}