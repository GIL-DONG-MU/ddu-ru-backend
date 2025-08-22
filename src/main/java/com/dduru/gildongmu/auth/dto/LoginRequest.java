package com.dduru.gildongmu.auth.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        String idToken
) {
}
