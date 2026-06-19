package com.dduru.gildongmu.chat.event;

/**
 * 그룹 채팅방 멤버 구성이 바뀌어 채팅방 목록의 참여자 수 또는 노출 여부를 갱신해야 함을 알린다.
 * <p>
 * 멤버 제거 이벤트는 남은 멤버에게 {@code UPSERT}, 제거된 사용자에게 {@code REMOVE}를 발행하는 데 사용된다.
 */
public record ChatMemberChangedEvent(
        Long roomId,
        Long memberUserId,
        ChatMemberChangeType changeType
) {
}
