package com.dduru.gildongmu.recommendation.dto.result;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.MateRecommendationCardQueryResult;

import java.time.LocalDate;
import java.util.List;

public record MateRecommendationQueryResult(
        RecommendationAvailabilityStatus availabilityStatus,
        LocalDate referenceDate,
        List<MateRecommendationCardQueryResult> recommendations
) {

    public static MateRecommendationQueryResult surveyRequired() {
        return new MateRecommendationQueryResult(
                RecommendationAvailabilityStatus.SURVEY_REQUIRED,
                null,
                List.of()
        );
    }

    public static MateRecommendationQueryResult available(
            LocalDate referenceDate,
            List<MateRecommendationCardQueryResult> recommendations
    ) {
        return new MateRecommendationQueryResult(
                RecommendationAvailabilityStatus.AVAILABLE,
                referenceDate,
                recommendations
        );
    }

    public boolean requiresSurvey() {
        return availabilityStatus == RecommendationAvailabilityStatus.SURVEY_REQUIRED;
    }
}
