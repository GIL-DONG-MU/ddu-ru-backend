package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

public record JourneyGalleryRequest(
        LocalDateTime cursorCreatedAt,
        Long cursorPostId,
        Integer cursorSortOrder,
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
