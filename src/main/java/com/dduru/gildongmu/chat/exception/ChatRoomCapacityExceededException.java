package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatRoomCapacityExceededException extends BusinessException {
    public ChatRoomCapacityExceededException() {
        super(ErrorCode.CHAT_ROOM_CAPACITY_EXCEEDED);
    }
}
