package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MateRecommendationItemResponse(
        Long recommendationId,
        Long postId,
        int matchPercentage,
        String title,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        HostResponse host,
        int currentMemberCount,
        int maxMemberCount,
        String description,
        List<String> tags
) {
}
