package com.dduru.gildongmu.chat.dto.ws.roomlist;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomLastMessageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 실시간 이벤트 안에 포함되는 채팅방 item 표현이다.
 * <p>
 * {@code UPSERT}에서는 REST 목록 item과 동일한 값을 담고, {@code REMOVE}에서는 제거 처리에 필요한 {@code chatRoomId},
 * {@code roomType}만 담는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅방 목록 이벤트 안의 채팅방 item. UPSERT는 전체 필드, REMOVE는 chatRoomId와 roomType만 포함합니다.")
public record ChatRoomListEventRoomPayload(
        @Schema(description = "채팅방 ID", example = "10")
        Long chatRoomId,

        @Schema(description = "채팅방 타입", example = "GROUP", allowableValues = {"PRIVATE", "GROUP"})
        ChatRoomType roomType,

        @Schema(description = "채팅방 상태. REMOVE 이벤트에서는 생략됩니다.", example = "ACTIVE", allowableValues = {"ACTIVE", "CLOSED", "DELETED"}, nullable = true)
        ChatRoomStatus status,

        @Schema(description = "목록 표시명. PRIVATE은 상대 유저 닉네임, GROUP은 여정 제목", example = "제주 애월 2박 3일 함께가기", nullable = true)
        String displayName,

        @Schema(description = "PRIVATE 채팅방의 게시글 제목. GROUP은 null", example = "제주 애월 2박 3일", nullable = true)
        String postTitle,

        @Schema(description = "목록 썸네일 URL", example = "https://example.com/images/chat-thumbnail.png", nullable = true)
        String thumbnailUrl,

        @Schema(description = "연결 게시글 ID", example = "101", nullable = true)
        Long postId,

        @Schema(description = "연결 여정 ID", example = "55", nullable = true)
        Long journeyId,

        @Schema(description = "현재 채팅방 멤버 수", example = "4", nullable = true)
        Integer participantCount,

        @Schema(description = "마지막으로 표시 가능한 메시지. 메시지가 없으면 null", nullable = true)
        ChatRoomLastMessageResponse lastMessage,

        @Schema(description = "현재 사용자가 아직 읽지 않은 메시지 수", example = "3", nullable = true)
        Long unreadCount,

        @Schema(description = "현재 사용자의 마지막 읽음 메시지 ID", example = "120", nullable = true)
        Long lastReadMessageId,

        @Schema(description = "목록 정렬 기준이 되는 마지막 활동 시각", nullable = true)
        LocalDateTime activityAt,

        @Schema(description = "채팅방 생성 시각", nullable = true)
        LocalDateTime createdAt
) {
    public static ChatRoomListEventRoomPayload from(ChatRoomListItemResponse chatRoom) {
        return new ChatRoomListEventRoomPayload(
                chatRoom.chatRoomId(),
                chatRoom.roomType(),
                chatRoom.status(),
                chatRoom.displayName(),
                chatRoom.postTitle(),
                chatRoom.thumbnailUrl(),
                chatRoom.postId(),
                chatRoom.journeyId(),
                chatRoom.participantCount(),
                chatRoom.lastMessage(),
                chatRoom.unreadCount(),
                chatRoom.lastReadMessageId(),
                chatRoom.activityAt(),
                chatRoom.createdAt()
        );
    }

    public static ChatRoomListEventRoomPayload removed(Long chatRoomId, ChatRoomType roomType) {
        return ChatRoomListEventRoomPayload.builder()
                .chatRoomId(chatRoomId)
                .roomType(roomType)
                .build();
    }
}
