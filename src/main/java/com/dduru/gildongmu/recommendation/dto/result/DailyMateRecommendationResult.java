package com.dduru.gildongmu.recommendation.dto.result;

import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;

public record DailyMateRecommendationResult(
        RecommendationAvailabilityStatus availabilityStatus,
        Long batchId,
        MateRecommendationBatchStatus batchStatus,
        RecommendationApplicantContext applicantContext
) {
    public static DailyMateRecommendationResult surveyRequired() {
        return new DailyMateRecommendationResult(
                RecommendationAvailabilityStatus.SURVEY_REQUIRED,
                null,
                null,
                null
        );
    }

    public static DailyMateRecommendationResult available(
            Long batchId,
            MateRecommendationBatchStatus batchStatus,
            RecommendationApplicantContext applicantContext
    ) {
        return new DailyMateRecommendationResult(
                RecommendationAvailabilityStatus.AVAILABLE,
                batchId,
                batchStatus,
                applicantContext
        );
    }

    public static DailyMateRecommendationResult generating(
            Long batchId,
            RecommendationApplicantContext applicantContext
    ) {
        return new DailyMateRecommendationResult(
                RecommendationAvailabilityStatus.GENERATING,
                batchId,
                MateRecommendationBatchStatus.CREATED,
                applicantContext
        );
    }

    public boolean hasCompletedRecommendations() {
        return batchStatus == MateRecommendationBatchStatus.COMPLETED;
    }
}
