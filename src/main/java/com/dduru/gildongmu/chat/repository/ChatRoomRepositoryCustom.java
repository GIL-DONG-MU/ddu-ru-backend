package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListQueryResult;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {

    List<ChatRoomListQueryResult> findActiveListPageByUserId(
            Long userId,
            ChatRoomType roomType,
            ChatRoomListCursor cursor,
            Pageable pageable
    );

    Optional<ChatRoomListQueryResult> findActiveListItemByUserIdAndRoomId(Long userId, Long roomId);

    List<ChatMessage> findLastVisibleMessagesByRoomIds(Long userId, Collection<Long> roomIds);

    Map<Long, Long> countUnreadMessagesByRoomIds(Long userId, Collection<Long> roomIds);

    Map<Long, Long> countMembersByRoomIds(Collection<Long> roomIds);
}
