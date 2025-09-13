package com.dduru.gildongmu.auth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserResponse(
        String id,
        String email,
        String name,
        String picture,
        String locale,
        @JsonProperty("verified_email")
        boolean verifiedEmail
) {
}
