package com.dduru.gildongmu.journey.dto.request;

public record JourneyPostListRequest(
        Long cursor,
        Integer size
) {
    public static final int DEFAULT_SIZE = 20;

    public JourneyPostListRequest {
        if (size == null || size <= 0 || size > 50) size = DEFAULT_SIZE;
    }
}
