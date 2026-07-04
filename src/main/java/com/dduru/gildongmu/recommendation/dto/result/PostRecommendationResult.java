package com.dduru.gildongmu.recommendation.dto.result;

import java.util.List;

public record PostRecommendationResult(
        boolean available,
        List<ScoredPostRecommendation> recommendations
) {
    public static PostRecommendationResult unavailable() {
        return new PostRecommendationResult(false, List.of());
    }

    public static PostRecommendationResult available(List<ScoredPostRecommendation> recommendations) {
        return new PostRecommendationResult(true, recommendations);
    }
}
