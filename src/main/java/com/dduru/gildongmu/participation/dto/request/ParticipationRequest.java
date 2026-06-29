package com.dduru.gildongmu.participation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ParticipationRequest(
        @Schema(description = "동행 신청 메시지. 선택 입력이며 최대 500자입니다.", example = "안녕하세요. 일정이 비슷해서 신청드립니다.", nullable = true)
        @Size(max = 500, message = "신청 메시지는 500자 이하여야 합니다")
        String message
) {
}
