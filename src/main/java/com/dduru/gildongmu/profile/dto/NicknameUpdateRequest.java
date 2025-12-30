package com.dduru.gildongmu.profile.dto;

import com.dduru.gildongmu.profile.validator.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameUpdateRequest(
        @Schema(
                description = "닉네임",
                example = "길동무",
                minLength = 2,
                maxLength = 14
        )
        @ValidNickname
        String nickname
) {
}
