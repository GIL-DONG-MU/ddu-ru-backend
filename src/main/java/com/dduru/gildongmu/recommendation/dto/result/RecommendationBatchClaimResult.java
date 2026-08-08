package com.dduru.gildongmu.recommendation.dto.result;

import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;

public record RecommendationBatchClaimResult(
        Long batchId,
        MateRecommendationBatchStatus status,
        boolean claimed
) {
    public static RecommendationBatchClaimResult claimed(Long batchId) {
        return new RecommendationBatchClaimResult(batchId, MateRecommendationBatchStatus.CREATED, true);
    }

    public static RecommendationBatchClaimResult existing(Long batchId, MateRecommendationBatchStatus status) {
        return new RecommendationBatchClaimResult(batchId, status, false);
    }
}
