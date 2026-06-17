package com.dduru.gildongmu.chat.dto.ws.roomlist;

/**
 * 채팅방 목록 실시간 이벤트가 클라이언트 목록에 적용되는 방식을 구분한다.
 * <p>
 * {@code UPSERT}는 채팅방 item을 삽입하거나 교체하고, {@code REMOVE}는 목록에서 제거한다.
 */
public enum ChatRoomListEventType {
    UPSERT,
    REMOVE
}
