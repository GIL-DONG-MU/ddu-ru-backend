package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SelfParticipationNotAllowedException extends BusinessException {
    
    public SelfParticipationNotAllowedException() {
        super(ErrorCode.SELF_PARTICIPATION_NOT_ALLOWED,"자신의 게시글에 참여할 수 없습니다");
    }
    public SelfParticipationNotAllowedException(String message) {
        super(ErrorCode.SELF_PARTICIPATION_NOT_ALLOWED, message);
    }
}
