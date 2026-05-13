package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record JourneyPostListRequest(
        @Positive(message = "cursor는 1 이상이어야 합니다.")
        Long cursor,

        @Min(value = 1, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        @Max(value = 50, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        Integer size
) {
    public static final int DEFAULT_SIZE = 20;

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }
}
