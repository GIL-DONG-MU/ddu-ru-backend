package com.dduru.gildongmu.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
        @Schema(
                description = "닉네임",
                example = "길동무",
                minLength = 2,
                maxLength = 14
        )
        @NotBlank(message = "닉네임은 공백일 수 없습니다.")
        String nickname
) {
}
