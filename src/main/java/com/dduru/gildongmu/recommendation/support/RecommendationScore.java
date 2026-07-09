package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.recommendation.dto.result.RecommendationReason;

import java.util.List;

public record RecommendationScore(
        int matchPercentage,
        List<RecommendationReason> matchReasons,
        List<RecommendationReason> cautionPoints
) {
}
