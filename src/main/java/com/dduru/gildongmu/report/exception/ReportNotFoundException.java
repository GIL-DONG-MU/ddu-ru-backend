package com.dduru.gildongmu.report.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ReportNotFoundException extends BusinessException {

    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }
}
