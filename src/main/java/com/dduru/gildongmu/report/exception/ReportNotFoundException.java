package com.dduru.gildongmu.report.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ReportNotFoundException extends BusinessException {

    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }

    public ReportNotFoundException(String message) {
        super(ErrorCode.REPORT_NOT_FOUND, message);
    }

    public static ReportNotFoundException of(Long reportId) {
        return new ReportNotFoundException("신고 내역을 찾을 수 없습니다. reportId=" + reportId);
    }
}
