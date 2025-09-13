package com.dduru.gildongmu.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,

        @JsonProperty("connected_at")
        String connectedAt,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
}
