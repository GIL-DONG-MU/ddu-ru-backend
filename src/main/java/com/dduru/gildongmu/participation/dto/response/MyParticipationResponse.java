package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 보낸 동행 신청(신청 내역) 목록 항목")
public record MyParticipationResponse(
        @Schema(description = "참여 신청 ID", example = "12")
        Long participationId,
        @Schema(description = "게시글 ID", example = "101")
        Long postId,
        @Schema(description = "게시글 제목", example = "제주도 힐링 여행")
        String postTitle,
        @Schema(description = "게시글 대표 이미지 URL")
        String photoUrl,
        @Schema(description = "내 신청 화면 기준 상태", allowableValues = {"PENDING", "CONTACTING", "APPROVED", "REJECTED", "REMOVED_BY_HOST"})
        MyParticipationStatus status,
        @Schema(description = "신청 시각(상대 시각은 클라이언트에서 계산)")
        LocalDateTime appliedAt,
        @Schema(description = "대기 중일 때만 신청 취소 가능")
        boolean cancellable,
        @Schema(description = "연락 중(1:1) 채팅방 ID. `CONTACTING`일 때만 존재")
        Long privateRoomId,
        @Schema(description = "그룹 동행 채팅방 ID. `APPROVED`일 때만 존재")
        Long groupRoomId
) {
    public static MyParticipationResponse from(
            Participation participation,
            MyParticipationStatus status,
            Long privateRoomId,
            Long groupRoomId
    ) {
        Post post = participation.getPost();
        ParticipationStatus participationStatus = participation.getStatus();
        return new MyParticipationResponse(
                participation.getId(),
                post.getId(),
                post.getTitle(),
                post.getPhotoUrl(),
                status,
                participation.getCreatedAt(),
                participationStatus == ParticipationStatus.PENDING,
                privateRoomId,
                groupRoomId
        );
    }
}
