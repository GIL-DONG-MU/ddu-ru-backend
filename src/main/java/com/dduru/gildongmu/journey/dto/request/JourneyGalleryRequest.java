package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.AssertTrue;

public record JourneyGalleryRequest(
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

    @AssertTrue(message = "cursorPostId와 cursorSortOrder는 함께 입력하거나 함께 생략해야 합니다.")
    public boolean isCursorValid() {
        return (cursorPostId == null) == (cursorSortOrder == null);
    }
}
