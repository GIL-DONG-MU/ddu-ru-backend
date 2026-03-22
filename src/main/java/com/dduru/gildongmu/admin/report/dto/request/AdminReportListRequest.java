package com.dduru.gildongmu.admin.report.dto.request;

import com.dduru.gildongmu.report.domain.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 신고 목록 조회 조건")
public record AdminReportListRequest(
        @Schema(description = "처리 상태(미지정 시 전체)")
        ReportStatus status
) {
}
