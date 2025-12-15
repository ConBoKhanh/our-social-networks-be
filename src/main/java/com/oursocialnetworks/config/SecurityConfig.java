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
    private final CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;

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
                                "/register",                 // Trang đăng ký HTML
                                "/forgot-password",          // Trang quên mật khẩu HTML
                                "/change-password",          // Trang đổi mật khẩu HTML
                                "/processing",               // Trang loading khi tạo tài khoản
                                "/oauth2/**",                // OAuth2 endpoints
                                "/login/oauth2/**",          // OAuth2 callback
                                "/auth/login",               // JWT login endpoint
                                "/auth/login/basic",         // Username/password login endpoint
                                "/auth/change-password-new-user", // Change password for new users (no auth)
                                "/auth/register/**",         // Registration endpoints
                                "/auth/forgot-password/**",  // Forgot password endpoints
                                "/auth/callback",            // Auth callback endpoint
                                "/auth/oauth2/**"            // Auth OAuth2 endpoints
                        ).permitAll()

                        // ✅ Health Check - Public
                        .requestMatchers(
                                "/api/health",
                                "/api/ping",
                                "/api/info",
                                "/api/debug/public-test",
                                "/api/debug/oauth2-config",
                                "/api/debug/test-redirect",
                                "/api/debug/oauth2-flow-info",
                                "/api/debug/email-config",
                                "/api/debug/email-preview/**",
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
                    .failureHandler((request, response, exception) -> {
                        System.err.println("========== OAuth2 LOGIN FAILURE ==========");
                        System.err.println("Request URL: " + request.getRequestURL());
                        System.err.println("Request URI: " + request.getRequestURI());
                        System.err.println("Error Type: " + exception.getClass().getSimpleName());
                        System.err.println("Error Message: " + exception.getMessage());
                        System.err.println("Request Parameters:");
                        request.getParameterMap().forEach((key, values) -> 
                            System.err.println("  " + key + ": " + String.join(", ", values)));
                        exception.printStackTrace();
                        System.err.println("==========================================");
                        
                        String errorMsg = exception.getMessage();
                        if (errorMsg == null || errorMsg.trim().isEmpty()) {
                            errorMsg = "OAuth2 authentication failed";
                        }
                        response.sendRedirect("/login?error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8));
                    })
                    .authorizationEndpoint(authorization -> authorization
                            .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                    )
                    .permitAll()
            );
        }
        }

        return http.build();
    }
}