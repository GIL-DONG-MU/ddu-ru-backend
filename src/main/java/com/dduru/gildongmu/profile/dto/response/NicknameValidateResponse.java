package com.dduru.gildongmu.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NicknameValidateResponse(
        @Schema(description = "검증 및 정규화가 완료된 닉네임", example = "여행메이트")
        String sanitizedNickname
) {
}
