package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyPostCommentException extends BusinessException {

    private InvalidJourneyPostCommentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidJourneyPostCommentException emptyPatch() {
        return new InvalidJourneyPostCommentException(ErrorCode.JOURNEY_POST_COMMENT_EMPTY_PATCH);
    }

    public static InvalidJourneyPostCommentException invalidContent() {
        return new InvalidJourneyPostCommentException(ErrorCode.JOURNEY_POST_COMMENT_INVALID_CONTENT);
    }
}
