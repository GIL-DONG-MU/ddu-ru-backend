package com.dduru.gildongmu.chat.event;

/**
 * 사용자 메시지가 저장되어 채팅방 목록의 마지막 메시지, unread count, 정렬 기준을 갱신해야 함을 알린다.
 * <p>
 * 이벤트는 최신 상태를 직접 담지 않고, 커밋 이후 재조회에 필요한 식별자만 전달한다.
 */
public record ChatMessageCreatedEvent(
        Long roomId,
        Long messageId
) {
}
