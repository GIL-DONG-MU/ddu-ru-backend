package com.dduru.gildongmu.chat.event;

/**
 * 나의 여정 기본 정보가 바뀌어 그룹 채팅방 목록의 제목 또는 대표 이미지를 갱신해야 함을 알린다.
 */
public record JourneyBasicInfoUpdatedEvent(
        Long journeyId
) {
}
