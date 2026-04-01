package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 받은 동행 신청 목록 응답")
public record ParticipationRetrieveResponse(
        @Schema(description = "참여 신청 ID", example = "12")
        Long participationId,
        @Schema(description = "신청자 ID", example = "33")
        Long userId,
        @Schema(description = "신청자 이름", example = "여행메이트")
        String userName,
        @Schema(description = "신청 메시지", example = "안녕하세요. 일정이 비슷해서 신청드립니다.")
        String message,
        @Schema(description = "신청 상태", example = "PENDING", allowableValues = {"PENDING", "CONTACTING", "APPROVED", "REJECTED"})
        ParticipationStatus status,
        @Schema(description = "신청 시각")
        LocalDateTime appliedAt,
        @Schema(description = "연락중 전환 시각")
        LocalDateTime contactedAt,
        @Schema(description = "승인 시각")
        LocalDateTime approvedAt,
        @Schema(description = "거절 시각")
        LocalDateTime rejectedAt,
        @Schema(description = "게시글 ID", example = "101")
        Long postId,
        @Schema(description = "게시글 제목", example = "제주도 같이 가실 분")
        String postTitle
) {

}
