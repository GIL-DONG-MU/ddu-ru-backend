package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.user.validator.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateNicknameRequest(
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
