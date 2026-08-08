package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.MateRecommendationCardQueryResult;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.MateRecommendationQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MateRecommendationQueryService {

    private final DailyMateRecommendationService dailyMateRecommendationService;
    private final MateRecommendationCardQueryService recommendationCardQueryService;

    public MateRecommendationQueryResult retrieve(Long userId) {
        DailyMateRecommendationResult dailyResult = dailyMateRecommendationService.getOrCreate(userId);
        if (dailyResult.availabilityStatus() == RecommendationAvailabilityStatus.SURVEY_REQUIRED) {
            return MateRecommendationQueryResult.surveyRequired();
        }
        if (!dailyResult.hasCompletedRecommendations()) {
            return MateRecommendationQueryResult.available(null, List.of());
        }

        List<MateRecommendationCardQueryResult> recommendations = recommendationCardQueryService.findVisibleCards(
                dailyResult.batchId(),
                userId,
                dailyResult.applicantContext()
        );
        return MateRecommendationQueryResult.available(
                dailyResult.applicantContext().today(),
                recommendations
        );
    }
}
