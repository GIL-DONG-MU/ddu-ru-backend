package com.dduru.gildongmu.report.dto.request;

import com.dduru.gildongmu.report.domain.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @Schema(description = "게시글 신고 사유", example = "SPAM")
        @NotNull(message = "신고 사유는 필수입니다")
        ReportReason reason,

        @Schema(description = "신고 상세 내용. 선택 입력이며 최대 1000자입니다.", example = "광고성 게시글로 보여요.", nullable = true)
        @Size(max = 1000, message = "상세 내용은 1000자 이하여야 합니다")
        String description
) {
}
