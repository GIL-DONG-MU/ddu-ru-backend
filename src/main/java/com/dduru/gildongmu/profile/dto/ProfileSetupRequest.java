package com.dduru.gildongmu.profile.dto;

import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.validator.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record ProfileSetupRequest(
        @Schema(description = "닉네임", example = "용감한여행자1234")
        @ValidNickname
        @NotBlank
        String nickname,
        
        @Schema(description = "성별", example = "M", allowableValues = {"M", "F"})
        @NotNull
        Gender gender,
        
        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phoneNumber,
        
        @Schema(description = "생년월일", example = "2000-01-01")
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식이 올바르지 않습니다. (예: 2000-01-01)")
        String birthday,
        
        @Schema(description = "전화번호 인증 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank
        String verificationToken
) {
}
