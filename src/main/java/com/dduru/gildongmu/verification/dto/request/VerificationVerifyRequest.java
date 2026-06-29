package com.dduru.gildongmu.verification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificationVerifyRequest(
        @Schema(description = "인증번호를 받은 휴대폰 번호. 하이픈 없이 010으로 시작하는 11자리 숫자입니다.", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phoneNumber,

        @Schema(description = "SMS로 발송된 6자리 인증번호", example = "123456")
        @NotBlank(message = "인증번호는 필수입니다.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {
}
