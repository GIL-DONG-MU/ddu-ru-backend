package com.dduru.gildongmu.admin.report.dto.request;

import com.dduru.gildongmu.report.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReportUpdateRequest(
        @NotNull(message = "처리 상태는 필수입니다.")
        ReportStatus status,

        @Size(max = 1000, message = "처리 메모는 1000자 이하여야 합니다.")
        String reviewNote
) {
}
