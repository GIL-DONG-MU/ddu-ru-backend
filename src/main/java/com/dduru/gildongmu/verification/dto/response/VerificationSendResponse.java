package com.dduru.gildongmu.verification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VerificationSendResponse(
        @Schema(description = "인증번호 만료 시각", example = "2026-06-29T12:39:56")
        LocalDateTime expiresAt
) {
}
