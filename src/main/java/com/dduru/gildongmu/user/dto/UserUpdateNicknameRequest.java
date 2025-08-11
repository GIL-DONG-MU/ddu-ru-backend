package com.dduru.gildongmu.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateNicknameRequest(
        @Size(max = 12, message = "닉네임은 12자 이내여야 합니다.")
        String nickname
) {
}
