package com.dduru.gildongmu.profile.dto;

import lombok.Builder;

@Builder
public record NicknameValidateResponse(
        String sanitizedNickname
) {
}
