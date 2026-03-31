package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatRoomNotFoundException extends BusinessException {
    public ChatRoomNotFoundException(String message) {
        super(ErrorCode.CHAT_ROOM_NOT_FOUND, message);
    }

    public static ChatRoomNotFoundException of(Long roomId) {
        return new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다. roomId=" + roomId);
    }
}
