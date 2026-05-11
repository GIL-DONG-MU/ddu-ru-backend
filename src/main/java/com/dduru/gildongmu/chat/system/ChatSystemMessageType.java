package com.dduru.gildongmu.chat.system;

public enum ChatSystemMessageType {
    USER_INVITED("%s 님이 그룹 채팅방에 참여했습니다."),
    USER_LEFT("%s 님이 채팅방을 나갔습니다."),
    USER_KICKED("%s 님이 채팅방에서 내보내졌습니다."),
    ROOM_CLOSED("채팅방이 닫혔습니다.");

    private final String displayTextTemplate;

    ChatSystemMessageType(String displayTextTemplate) {
        this.displayTextTemplate = displayTextTemplate;
    }

    public String getDisplayTextTemplate() {
        return displayTextTemplate;
    }
}
