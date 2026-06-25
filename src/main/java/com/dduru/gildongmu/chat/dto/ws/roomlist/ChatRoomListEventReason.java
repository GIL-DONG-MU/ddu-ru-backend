package com.dduru.gildongmu.chat.dto.ws.roomlist;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채팅방 목록 item이 변경된 도메인 원인을 나타낸다.
 * <p>
 * 클라이언트가 동일한 {@link ChatRoomListEventType} 안에서도 메시지, 읽음, 메타데이터, 멤버 변경을 구분할 수 있게 한다.
 */
@Schema(description = "채팅방 목록 실시간 이벤트 발생 원인")
public enum ChatRoomListEventReason {
    MESSAGE_CREATED,
    READ_UPDATED,
    ROOM_META_UPDATED,
    MEMBER_CHANGED
}
