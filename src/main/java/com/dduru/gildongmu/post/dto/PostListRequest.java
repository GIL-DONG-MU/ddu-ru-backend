package com.dduru.gildongmu.post.dto;

import java.time.LocalDate;

public record PostListRequest(
        Long cursor,
        Integer size,
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        String preferredGender,
        String preferredAge,
        Long destinationId,
        Boolean isRecruitOpen
) {
    public PostListRequest {
        if (size == null || size <= 0 || size > 50) size = 10;
    }
}
