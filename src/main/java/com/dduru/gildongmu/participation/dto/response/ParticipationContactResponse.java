package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여 신청 연락 시작 결과")
public record ParticipationContactResponse(
        @Schema(description = "참여 신청 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        Long participationId,
        @Schema(description = "신청자 회원 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        Long participantUserId,
        @Schema(description = "1:1 연락용 채팅방 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        Long privateRoomId,
        @Schema(description = "변경 후 참여 신청 상태", requiredMode = Schema.RequiredMode.REQUIRED)
        ParticipationStatus status
) {
}
