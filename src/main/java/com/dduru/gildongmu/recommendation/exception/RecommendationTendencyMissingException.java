package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecommendationTendencyMissingException extends BusinessException {
    public RecommendationTendencyMissingException() {
        super(ErrorCode.RECOMMENDATION_TENDENCY_MISSING);
    }
}
