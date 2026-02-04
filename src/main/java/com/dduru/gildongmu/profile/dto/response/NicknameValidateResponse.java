package com.dduru.gildongmu.profile.dto.response;

import lombok.Builder;

@Builder
public record NicknameValidateResponse(
        String sanitizedNickname
) {
}
