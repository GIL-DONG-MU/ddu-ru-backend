package com.dduru.gildongmu.verification.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record VerificationSendResponse(
        LocalDateTime expiresAt
) {
}
