package com.oursocialnetworks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body for refreshing access token")
public class RefreshTokenRequest {

    @Schema(
            description = "Refresh token received during login",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM...",
            required = true
    )
    private String refreshToken;
}