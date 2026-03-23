package com.dduru.gildongmu.report.dto.response;

import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.domain.enums.ReportStatus;

public record ReportCreateResponse(
        Long reportId,
        ReportStatus status
) {
    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(
                report.getId(),
                report.getStatus()
        );
    }
}
