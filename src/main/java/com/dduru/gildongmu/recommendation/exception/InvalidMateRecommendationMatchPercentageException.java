package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidMateRecommendationMatchPercentageException extends BusinessException {

    public InvalidMateRecommendationMatchPercentageException(int minPercentage, int maxPercentage) {
        super(
                ErrorCode.INVALID_MATE_RECOMMENDATION_MATCH_PERCENTAGE,
                "추천 일치율은 %d 이상 %d 이하여야 합니다.".formatted(minPercentage, maxPercentage)
        );
    }
}
