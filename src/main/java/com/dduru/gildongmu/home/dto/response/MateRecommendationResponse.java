package com.dduru.gildongmu.home.dto.response;

import java.util.List;

public record MateRecommendationResponse(
        boolean isAvailable,
        int remainingFreeCount,
        List<MateRecommendationItemResponse> recommendations
) {
}
