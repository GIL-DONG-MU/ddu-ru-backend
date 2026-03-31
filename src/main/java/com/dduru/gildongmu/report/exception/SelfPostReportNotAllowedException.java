package com.dduru.gildongmu.report.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SelfPostReportNotAllowedException extends BusinessException {

    public SelfPostReportNotAllowedException() {
        super(ErrorCode.SELF_POST_REPORT_NOT_ALLOWED);
    }
}
