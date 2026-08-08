package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidMateRecommendationRankException extends BusinessException {

    public InvalidMateRecommendationRankException(int minRank, int maxRank) {
        super(
                ErrorCode.INVALID_MATE_RECOMMENDATION_RANK,
                "추천 순위는 %d 이상 %d 이하여야 합니다.".formatted(minRank, maxRank)
        );
    }
}
