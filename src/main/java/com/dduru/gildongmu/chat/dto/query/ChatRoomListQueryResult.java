package com.dduru.gildongmu.chat.dto.query;

import com.dduru.gildongmu.chat.domain.ChatRoomMember;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 조회용 query row.
 *
 * @param currentMember 해당 채팅방에서 현재 사용자에 해당하는 멤버 정보
 * @param activityAt 최근 활동 시각. 채팅방 목록 정렬과 cursor 생성 기준으로 사용한다.
 */
public record ChatRoomListQueryResult(
        ChatRoomMember currentMember,
        LocalDateTime activityAt
) {
}
