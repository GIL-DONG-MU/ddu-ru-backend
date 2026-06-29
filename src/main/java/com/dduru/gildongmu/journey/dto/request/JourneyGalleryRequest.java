package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

public record JourneyGalleryRequest(
        @Schema(description = "다음 페이지 조회용 생성 시각 커서. cursorPostId, cursorSortOrder와 함께 전달합니다.", example = "2026-06-29T12:34:56", nullable = true)
        LocalDateTime cursorCreatedAt,
        @Schema(description = "다음 페이지 조회용 게시글 ID 커서. cursorCreatedAt, cursorSortOrder와 함께 전달합니다.", example = "120", nullable = true)
        Long cursorPostId,
        @Schema(description = "다음 페이지 조회용 이미지 정렬 순서 커서. cursorCreatedAt, cursorPostId와 함께 전달합니다.", example = "0", nullable = true)
        Integer cursorSortOrder,
        @Schema(description = "페이지 크기. 생략하거나 1 미만이면 20, 50 초과이면 50으로 보정됩니다.", example = "20")
        Integer size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    public JourneyGalleryRequest {
        if (size == null || size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }

    @AssertTrue(message = "cursorCreatedAt, cursorPostId, cursorSortOrder는 함께 입력하거나 함께 생략해야 합니다.")
    public boolean isCursorValid() {
        boolean allNull = cursorCreatedAt == null && cursorPostId == null && cursorSortOrder == null;
        boolean allPresent = cursorCreatedAt != null && cursorPostId != null && cursorSortOrder != null;
        return allNull || allPresent;
    }
}
