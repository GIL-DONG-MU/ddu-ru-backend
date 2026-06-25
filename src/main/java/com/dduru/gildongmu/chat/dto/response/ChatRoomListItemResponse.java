package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 목록 항목")
public record ChatRoomListItemResponse(
        @Schema(description = "채팅방 ID", example = "10")
        Long chatRoomId,

        @Schema(description = "채팅방 타입. PRIVATE은 1:1 채팅방, GROUP은 여정 그룹 채팅방", example = "PRIVATE", allowableValues = {"PRIVATE", "GROUP"})
        ChatRoomType roomType,

        @Schema(description = "채팅방 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "CLOSED", "DELETED"})
        ChatRoomStatus status,

        @Schema(description = "채팅 목록 표시명. PRIVATE은 상대 유저 닉네임, GROUP은 여정 제목", example = "여행메이트")
        String displayName,

        @Schema(description = "PRIVATE 채팅방의 게시글 제목. GROUP은 null", example = "제주 애월 2박 3일")
        String postTitle,

        @Schema(description = "목록 썸네일 URL. PRIVATE은 상대 프로필 이미지, GROUP은 여정 이미지 또는 게시글 이미지", example = "https://example.com/images/chat-thumbnail.png")
        String thumbnailUrl,

        @Schema(description = "연결된 게시글 ID. PRIVATE에서 주로 사용하며 GROUP에서도 원 게시글 컨텍스트가 있으면 내려갈 수 있습니다.", example = "101")
        Long postId,

        @Schema(description = "연결된 여정 ID. GROUP에서 사용하며 PRIVATE은 null일 수 있습니다.", example = "55", nullable = true)
        Long journeyId,

        @Schema(description = "현재 채팅방 멤버 수", example = "4")
        int participantCount,

        @Schema(description = "마지막으로 표시 가능한 메시지. 메시지가 아직 없으면 null", nullable = true)
        ChatRoomLastMessageResponse lastMessage,

        @Schema(description = "현재 사용자가 아직 읽지 않은 메시지 수", example = "3")
        long unreadCount,

        @Schema(description = "현재 사용자의 마지막 읽음 메시지 ID. 아직 읽은 메시지가 없으면 null", example = "120", nullable = true)
        Long lastReadMessageId,

        @Schema(description = "목록 정렬 기준이 되는 마지막 활동 시각")
        LocalDateTime activityAt,

        @Schema(description = "채팅방 생성 시각")
        LocalDateTime createdAt
) {
}
