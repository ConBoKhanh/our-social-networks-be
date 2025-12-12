// ========================================
// File: TokenRequest.java
// Location: src/main/java/com/oursocialnetworks/dto/TokenRequest.java
// ========================================
package com.oursocialnetworks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body for Google ID Token login")
public class TokenRequest {

    @Schema(
            description = "Google OAuth2 ID Token received from Google Sign-In",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjdlMzA3N2M1MDU0...",
            required = true
    )
    private String idToken;
}