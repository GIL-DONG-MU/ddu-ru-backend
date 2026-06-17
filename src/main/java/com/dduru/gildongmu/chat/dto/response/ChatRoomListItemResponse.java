package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatRoomListItemResponse(
        Long chatRoomId,
        ChatRoomType roomType,
        ChatRoomStatus status,
        @Schema(description = "채팅 목록 표시명. PRIVATE은 상대 유저 닉네임, GROUP은 여정 제목", example = "여행메이트")
        String displayName,
        @Schema(description = "PRIVATE 채팅방의 게시글 제목. GROUP은 null", example = "제주 애월 2박 3일")
        String postTitle,
        String thumbnailUrl,
        Long postId,
        Long journeyId,
        int participantCount,
        ChatRoomLastMessageResponse lastMessage,
        long unreadCount,
        Long lastReadMessageId,
        LocalDateTime activityAt,
        LocalDateTime createdAt
) {
}
