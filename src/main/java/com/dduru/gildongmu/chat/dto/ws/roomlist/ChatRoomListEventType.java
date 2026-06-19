package com.dduru.gildongmu.chat.dto.ws.roomlist;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채팅방 목록 실시간 이벤트가 클라이언트 목록에 적용되는 방식을 구분한다.
 * <p>
 * {@code UPSERT}는 채팅방 item을 삽입하거나 교체하고, {@code REMOVE}는 목록에서 제거한다.
 */
@Schema(description = "채팅방 목록 실시간 이벤트 적용 방식. UPSERT는 삽입/교체, REMOVE는 제거입니다.")
public enum ChatRoomListEventType {
    UPSERT,
    REMOVE
}
