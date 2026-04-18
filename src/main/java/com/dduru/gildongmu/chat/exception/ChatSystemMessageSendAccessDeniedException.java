package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatSystemMessageSendAccessDeniedException extends BusinessException {
    public ChatSystemMessageSendAccessDeniedException() {
        super(ErrorCode.CHAT_SYSTEM_MESSAGE_SEND_ACCESS_DENIED);
    }
}
