package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class MateRecommendationNotFoundException extends BusinessException {
    public MateRecommendationNotFoundException() {
        super(ErrorCode.MATE_RECOMMENDATION_NOT_FOUND);
    }
}
