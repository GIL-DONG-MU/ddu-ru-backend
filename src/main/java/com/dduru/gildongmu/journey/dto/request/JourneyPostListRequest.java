package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record JourneyPostListRequest(
        @Schema(description = "다음 페이지 조회용 게시글 ID 커서. 첫 페이지에서는 생략합니다.", example = "120", nullable = true)
        Long cursor,
        @Schema(description = "페이지 크기. 생략하거나 1 미만 또는 50 초과이면 20으로 보정됩니다.", example = "20")
        Integer size
) {
    public static final int DEFAULT_SIZE = 20;

    public JourneyPostListRequest {
        if (size == null || size <= 0 || size > 50) size = DEFAULT_SIZE;
    }
}
