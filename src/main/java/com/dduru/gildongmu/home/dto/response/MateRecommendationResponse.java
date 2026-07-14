package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;

import java.util.List;

public record MateRecommendationResponse(
        RecommendationAvailabilityStatus availabilityStatus,
        int remainingFreeCount,
        List<MateRecommendationItemResponse> recommendations
) {
    public static MateRecommendationResponse surveyRequired() {
        return new MateRecommendationResponse(
                RecommendationAvailabilityStatus.SURVEY_REQUIRED,
                0,
                List.of()
        );
    }

    public static MateRecommendationResponse available(List<MateRecommendationItemResponse> recommendations) {
        return new MateRecommendationResponse(
                RecommendationAvailabilityStatus.AVAILABLE,
                0,
                recommendations
        );
    }
}
