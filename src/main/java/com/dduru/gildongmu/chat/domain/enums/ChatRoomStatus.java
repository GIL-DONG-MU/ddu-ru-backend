package com.dduru.gildongmu.chat.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상태. ACTIVE는 사용 가능, CLOSED는 닫힘, DELETED는 삭제되어 일반 조회 대상에서 제외됩니다.")
public enum ChatRoomStatus {
    ACTIVE,
    CLOSED,
    DELETED
}
