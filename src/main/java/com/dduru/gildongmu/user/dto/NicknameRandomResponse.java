package com.dduru.gildongmu.user.dto;

import lombok.Builder;

@Builder
public record NicknameRandomResponse(
        String nickname
) {
    public static NicknameRandomResponse of(String nickname) {
        return NicknameRandomResponse.builder()
                .nickname(nickname)
                .build();
    }
}
