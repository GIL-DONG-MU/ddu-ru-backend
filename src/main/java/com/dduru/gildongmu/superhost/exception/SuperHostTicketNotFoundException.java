package com.dduru.gildongmu.superhost.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SuperHostTicketNotFoundException extends BusinessException {
    public SuperHostTicketNotFoundException() {
        super(ErrorCode.SUPER_HOST_TICKET_NOT_FOUND);
    }
}
