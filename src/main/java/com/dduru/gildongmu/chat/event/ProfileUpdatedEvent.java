package com.dduru.gildongmu.chat.event;

/**
 * 사용자 프로필 정보가 바뀌어 이 사용자가 상대방으로 보이는 1:1 채팅방 목록 item을 갱신해야 함을 알린다.
 */
public record ProfileUpdatedEvent(
        Long userId
) {
}
