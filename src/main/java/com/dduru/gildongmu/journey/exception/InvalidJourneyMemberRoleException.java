package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyMemberRoleException extends BusinessException {

    private InvalidJourneyMemberRoleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidJourneyMemberRoleException invalidCustomRoleLabel() {
        return new InvalidJourneyMemberRoleException(ErrorCode.JOURNEY_MEMBER_INVALID_CUSTOM_ROLE_LABEL);
    }

    public static InvalidJourneyMemberRoleException roleLimitExceeded() {
        return new InvalidJourneyMemberRoleException(ErrorCode.JOURNEY_MEMBER_ROLE_LIMIT_EXCEEDED);
    }

    public static InvalidJourneyMemberRoleException duplicateCustomRoleLabel() {
        return new InvalidJourneyMemberRoleException(ErrorCode.JOURNEY_MEMBER_DUPLICATE_CUSTOM_ROLE_LABEL);
    }
}
