package com.dduru.gildongmu.auth.dto.local;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocalGoogleTokenResponse (
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("id_token")
        String idToken
) {
}
