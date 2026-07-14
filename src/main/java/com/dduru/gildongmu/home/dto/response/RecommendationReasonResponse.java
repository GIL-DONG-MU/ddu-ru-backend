package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.recommendation.dto.result.RecommendationReason;

public record RecommendationReasonResponse(
        String code,
        String message
) {
    public static RecommendationReasonResponse from(RecommendationReason reason) {
        return new RecommendationReasonResponse(reason.code(), reason.message());
    }
}
