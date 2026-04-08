package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class GroupChatAlreadyMemberException extends BusinessException {
    public GroupChatAlreadyMemberException() {
        super(ErrorCode.GROUP_CHAT_ALREADY_MEMBER);
    }
}
