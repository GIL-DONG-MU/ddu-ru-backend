package com.dduru.gildongmu.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        String idToken
) {
}
