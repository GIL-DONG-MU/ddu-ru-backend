package com.dduru.gildongmu.recommendation.dto.result;

import java.time.LocalDate;
import java.util.List;

public record ScoredPostRecommendation(
        Long postId,
        LocalDate startDate,
        LocalDate endDate,
        int matchPercentage,
        List<RecommendationReason> matchReasons,
        List<RecommendationReason> cautionPoints
) {
}
