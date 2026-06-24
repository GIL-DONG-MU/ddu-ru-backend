package com.dduru.gildongmu.fcm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 삭제 요청")
public record FcmTokenDeleteRequest(
        @Schema(description = "삭제할 FCM 디바이스 토큰", example = "eKxy3z1...")
        @NotBlank(message = "token은 필수입니다.")
        String token
) {
}
