package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.NotNull;

public record JourneyPostNoticeUpdateRequest(
        @NotNull(message = "공지 여부는 필수입니다.")
        Boolean isNotice
) {
}
