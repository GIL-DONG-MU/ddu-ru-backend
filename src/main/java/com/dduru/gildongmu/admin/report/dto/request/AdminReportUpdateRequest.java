package com.dduru.gildongmu.admin.report.dto.request;

import com.dduru.gildongmu.report.domain.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReportUpdateRequest(
        @Schema(description = "변경할 신고 처리 상태", example = "RESOLVED")
        @NotNull(message = "처리 상태는 필수입니다.")
        ReportStatus status,

        @Schema(description = "관리자 처리 메모. 최대 1000자입니다.", example = "게시글 삭제 처리 완료", nullable = true)
        @Size(max = 1000, message = "처리 메모는 1000자 이하여야 합니다.")
        String reviewNote
) {
}
