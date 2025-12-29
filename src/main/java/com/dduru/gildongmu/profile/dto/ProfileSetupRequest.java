package com.dduru.gildongmu.profile.dto;

import com.dduru.gildongmu.profile.validator.ValidNickname;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileSetupRequest(
        @ValidNickname
        @NotBlank
        String nickname,
        @NotBlank
        @Pattern(regexp = "^(M|F)$", message = "성별 형식이 올바르지 않습니다.")
        String gender,
        @NotBlank
        @Size(min = 11, max = 11, message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식이 올바르지 않습니다. (예: 2000-01-01)")
        String birthday,
        @NotBlank
        String verificationToken
) {
}
