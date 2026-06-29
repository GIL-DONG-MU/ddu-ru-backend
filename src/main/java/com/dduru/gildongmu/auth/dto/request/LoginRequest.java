package com.dduru.gildongmu.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @Schema(description = "소셜 OAuth 제공자가 발급한 ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...")
        @NotNull
        String idToken
) {
}
