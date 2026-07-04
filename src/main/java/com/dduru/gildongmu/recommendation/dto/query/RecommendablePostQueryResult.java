package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.post.domain.enums.CompanionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecommendablePostQueryResult(
        Long postId,
        LocalDate startDate,
        LocalDate endDate,
        CompanionType companionType,
        BigDecimal hostRhythmScore,
        BigDecimal hostEnergyScore,
        BigDecimal hostConsumptionScore,
        BigDecimal hostDecisionScore
) {
}
