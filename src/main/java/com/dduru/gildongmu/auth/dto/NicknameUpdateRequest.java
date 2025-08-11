package com.dduru.gildongmu.auth.dto;

import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}