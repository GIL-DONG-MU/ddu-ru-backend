package com.dduru.gildongmu.chat.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 타입. PRIVATE은 게시글 기준 1:1 채팅방, GROUP은 여정 기준 그룹 채팅방입니다.")
public enum ChatRoomType {
    PRIVATE,
    GROUP
}
