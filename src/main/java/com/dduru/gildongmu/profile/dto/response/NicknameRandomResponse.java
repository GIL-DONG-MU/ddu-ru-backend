package com.dduru.gildongmu.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NicknameRandomResponse(
        @Schema(description = "사용 가능한 랜덤 닉네임", example = "푸른여행자1234")
        String nickname
) {
    public static NicknameRandomResponse of(String nickname) {
        return NicknameRandomResponse.builder()
                .nickname(nickname)
                .build();
    }
}
