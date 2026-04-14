package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidParticipationStatusException extends BusinessException {
    public InvalidParticipationStatusException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidParticipationStatusException contactNotAllowed() {
        return new InvalidParticipationStatusException(ErrorCode.PARTICIPATION_CONTACT_NOT_ALLOWED);
    }

    public static InvalidParticipationStatusException approvalNotAllowed() {
        return new InvalidParticipationStatusException(ErrorCode.PARTICIPATION_APPROVAL_NOT_ALLOWED);
    }

    public static InvalidParticipationStatusException rejectionNotAllowed() {
        return new InvalidParticipationStatusException(ErrorCode.PARTICIPATION_REJECTION_NOT_ALLOWED);
    }

    public static InvalidParticipationStatusException cancelNotAllowed() {
        return new InvalidParticipationStatusException(ErrorCode.PARTICIPATION_CANCEL_NOT_ALLOWED);
    }

}
