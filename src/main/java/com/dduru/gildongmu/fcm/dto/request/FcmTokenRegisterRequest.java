package com.dduru.gildongmu.fcm.dto.request;

import com.dduru.gildongmu.fcm.domain.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "FCM 토큰 등록/갱신 요청")
public record FcmTokenRegisterRequest(
        @Schema(description = "FCM 디바이스 토큰", example = "eKxy3z1...")
        @NotBlank(message = "token은 필수입니다.")
        String token,
        @Schema(description = "기기 타입", example = "AOS")
        @NotNull(message = "deviceType은 필수입니다.")
        DeviceType deviceType
) {
}
