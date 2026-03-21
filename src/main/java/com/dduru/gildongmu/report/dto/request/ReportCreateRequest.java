package com.dduru.gildongmu.report.dto.request;

import com.dduru.gildongmu.report.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull(message = "신고 사유는 필수입니다")
        ReportReason reason,

        @Size(max = 1000, message = "상세 내용은 1000자 이하여야 합니다")
        String description
) {
}
