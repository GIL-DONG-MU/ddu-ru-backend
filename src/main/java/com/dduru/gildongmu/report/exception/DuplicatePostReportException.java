package com.dduru.gildongmu.report.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicatePostReportException extends BusinessException {

    public DuplicatePostReportException() {
        super(ErrorCode.DUPLICATE_POST_REPORT);
    }

    public DuplicatePostReportException(String message) {
        super(ErrorCode.DUPLICATE_POST_REPORT, message);
    }
}
