package com.dduru.gildongmu.chat.dto.query;

/**
 * 특정 채팅방 목록 변경을 전달해야 하는 사용자 대상을 표현한다.
 * <p>
 * 현재는 프로필 변경처럼 "변경된 사용자"와 "목록을 갱신해야 하는 사용자"가 다른 경우에 사용한다.
 */
public record ChatRoomUserTargetQueryResult(
        Long chatRoomId,
        Long userId
) {
}
