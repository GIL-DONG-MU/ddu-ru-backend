package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.user.validator.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateNicknameRequest(
        @Schema(
                description = "닉네임 (2~12자, 한글/영문/숫자/공백만 허용, 연속 공백 불가)",
                example = "길동무",
                minLength = 2,
                maxLength = 12
        )
        @ValidNickname
        String nickname
) {
}
