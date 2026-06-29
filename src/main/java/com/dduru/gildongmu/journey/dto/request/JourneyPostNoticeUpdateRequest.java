package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record JourneyPostNoticeUpdateRequest(
        @Schema(description = "공지 설정 여부. true이면 공지로 등록하고 false이면 공지를 해제합니다.", example = "true")
        @NotNull(message = "공지 여부는 필수입니다.")
        Boolean isNotice
) {
}
