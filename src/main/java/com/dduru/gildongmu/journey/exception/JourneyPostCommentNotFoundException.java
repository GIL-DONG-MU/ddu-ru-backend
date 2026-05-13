package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyPostCommentNotFoundException extends BusinessException {

    public JourneyPostCommentNotFoundException() {
        super(ErrorCode.JOURNEY_POST_COMMENT_NOT_FOUND);
    }
}
