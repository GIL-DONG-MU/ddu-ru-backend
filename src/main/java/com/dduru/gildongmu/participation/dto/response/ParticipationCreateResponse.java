package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record ParticipationCreateResponse(
        @Schema(description = "생성된 참여 신청 ID", example = "12")
        Long participationId,
        @Schema(description = "생성 직후 참여 신청 상태", example = "PENDING", allowableValues = {"PENDING", "CONTACTING", "APPROVED", "REJECTED"})
        ParticipationStatus status
) {
}
