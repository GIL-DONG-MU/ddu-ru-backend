package com.dduru.gildongmu.chat.event;

/**
 * 여행 모집글 정보가 바뀌어 연결된 1:1 채팅방과 그룹 채팅방의 목록 메타데이터를 갱신해야 함을 알린다.
 */
public record PostUpdatedEvent(
        Long postId
) {
}
