package com.dduru.gildongmu.chat.event;

/**
 * 사용자의 읽음 위치가 실제로 앞으로 이동해 해당 사용자 채팅방 목록의 unread count를 갱신해야 함을 알린다.
 */
public record ChatReadUpdatedEvent(
        Long roomId,
        Long readerUserId
) {
}
