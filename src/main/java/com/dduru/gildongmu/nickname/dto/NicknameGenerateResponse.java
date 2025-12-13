package com.dduru.gildongmu.nickname.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "닉네임 생성 응답")
@Builder
public record NicknameGenerateResponse(

        @Schema(description = "생성된 닉네임", example = "용감한 여행자")
        String nickname

) {
    public static NicknameGenerateResponse of(String nickname) {
        return NicknameGenerateResponse.builder()
                .nickname(nickname)
                .build();
    }
}
