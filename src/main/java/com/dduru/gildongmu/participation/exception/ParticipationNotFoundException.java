package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ParticipationNotFoundException extends BusinessException {
    public ParticipationNotFoundException() {
        super(ErrorCode.PARTICIPATION_NOT_FOUND);
    }

    public ParticipationNotFoundException(String message) {
        super(ErrorCode.PARTICIPATION_NOT_FOUND, message);
    }

    public static ParticipationNotFoundException of(Long participationId) {
        return new ParticipationNotFoundException("참여신청을 찾을 수 없습니다. participationId=" + participationId);
    }
}
