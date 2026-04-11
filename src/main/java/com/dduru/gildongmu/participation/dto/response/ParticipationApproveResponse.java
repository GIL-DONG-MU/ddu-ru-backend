package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여 신청 승인 결과")
public record ParticipationApproveResponse(
        @Schema(description = "참여 신청 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        Long participationId,
        @Schema(description = "승인된 신청자 회원 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        Long inviteeUserId,
        @Schema(description = "동행 단톡(그룹) 채팅방 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
        Long groupRoomId,
        @Schema(description = "변경 후 참여 신청 상태", requiredMode = Schema.RequiredMode.REQUIRED)
        ParticipationStatus status
) {
}
