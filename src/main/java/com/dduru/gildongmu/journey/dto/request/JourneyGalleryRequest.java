package com.dduru.gildongmu.journey.dto.request;

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
}
