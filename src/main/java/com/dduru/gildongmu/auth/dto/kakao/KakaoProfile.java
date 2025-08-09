package com.dduru.gildongmu.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoProfile(
        String nickname,

        @JsonProperty("profile_image_url")
        String profileImageUrl,

        @JsonProperty("thumbnail_image_url")
        String thumbnailImageUrl
) {
}
