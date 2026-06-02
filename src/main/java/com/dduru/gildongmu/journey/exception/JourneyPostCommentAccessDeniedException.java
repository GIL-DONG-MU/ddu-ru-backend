package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyPostCommentAccessDeniedException extends BusinessException {

    public JourneyPostCommentAccessDeniedException() {
        super(ErrorCode.JOURNEY_POST_COMMENT_ACCESS_DENIED);
    }
}
