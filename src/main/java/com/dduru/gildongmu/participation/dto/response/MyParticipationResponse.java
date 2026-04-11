package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.post.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        @Schema(description = "여행지(도시)", example = "제주시")
        String destinationCity,
        @Schema(description = "여행 기간 표시", example = "2박 3일")
        String tripDurationText,
        @Schema(description = "신청 상태", allowableValues = {"PENDING", "CONTACTING", "APPROVED", "REJECTED"})
        ParticipationStatus status,
        @Schema(description = "상태 안내 문구(한글)")
        String statusMessage,
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
            Long privateRoomId,
            Long groupRoomId
    ) {
        Post post = participation.getPost();
        ParticipationStatus status = participation.getStatus();
        return new MyParticipationResponse(
                participation.getId(),
                post.getId(),
                post.getTitle(),
                post.getPhotoUrl(),
                post.getDestination().getCity(),
                tripDurationLabel(post.getStartDate(), post.getEndDate()),
                status,
                statusMessage(status),
                participation.getCreatedAt(),
                status == ParticipationStatus.PENDING,
                privateRoomId,
                groupRoomId
        );
    }

    private static String tripDurationLabel(LocalDate startDate, LocalDate endDate) {
        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        int totalDays = (int) nights + 1;
        return nights > 0 ? nights + "박 " + totalDays + "일" : "당일 일정";
    }

    private static String statusMessage(ParticipationStatus status) {
        return switch (status) {
            case PENDING -> "방장이 아직 확인하지 않았어요";
            case CONTACTING -> "방장과 대화 중이에요";
            case APPROVED -> "동행이 확정되었어요";
            case REJECTED -> "아쉽게도 거절되었어요";
        };
    }
}
