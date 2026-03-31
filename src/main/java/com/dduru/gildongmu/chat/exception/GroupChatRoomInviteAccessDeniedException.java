package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class GroupChatRoomInviteAccessDeniedException extends BusinessException {
    public GroupChatRoomInviteAccessDeniedException() {
        super(ErrorCode.CHAT_ROOM_INVITE_ACCESS_DENIED);
    }
}
