package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.dto.request.ChatRoomListType;

import java.util.List;

public record ChatRoomListResponse(
        ChatRoomListType selectedRoomType,
        List<ChatRoomListItemResponse> chatRooms,
        ChatRoomListPageResponse page
) {
}
