package com.dduru.gildongmu.user.dto;

import lombok.Builder;

@Builder
public record UserCheckNicknameResponse(
        String sanitizedNickname
) {
}
