package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.dto.request.ChatRoomListType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅방 목록 응답")
public record ChatRoomListResponse(
        @Schema(description = "실제로 적용된 채팅방 목록 필터", example = "ALL", allowableValues = {"ALL", "PRIVATE", "GROUP"})
        ChatRoomListType selectedRoomType,

        @Schema(description = "현재 사용자가 참여 중인 ACTIVE 채팅방 목록")
        List<ChatRoomListItemResponse> chatRooms,

        @Schema(description = "채팅방 목록 페이지 정보")
        ChatRoomListPageResponse page
) {
}
