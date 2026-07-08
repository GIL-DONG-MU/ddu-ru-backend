package com.dduru.gildongmu.recommendation.dto.result;

import java.util.List;

public record PostRecommendationResult(
        PostRecommendationResultStatus status,
        List<ScoredPostRecommendation> recommendations
) {
    public static PostRecommendationResult surveyRequired() {
        return new PostRecommendationResult(PostRecommendationResultStatus.SURVEY_REQUIRED, List.of());
    }

    public static PostRecommendationResult noCandidates() {
        return new PostRecommendationResult(PostRecommendationResultStatus.NO_CANDIDATES, List.of());
    }

    public static PostRecommendationResult ready(List<ScoredPostRecommendation> recommendations) {
        return new PostRecommendationResult(PostRecommendationResultStatus.READY, recommendations);
    }
}
