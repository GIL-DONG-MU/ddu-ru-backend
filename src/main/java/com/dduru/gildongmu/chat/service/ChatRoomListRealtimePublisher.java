package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomUserTargetQueryResult;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventPayload;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventReason;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 사용자별 채팅방 목록 item을 재계산해 개인 WebSocket queue로 발행한다.
 * <p>
 * 목록 item은 사용자별 unread count, 상대 프로필, 표시명처럼 개인화된 값을 포함하므로 발행 시점마다 대상 사용자 기준으로 다시 조립한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomListRealtimePublisher {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomListService chatRoomListService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TimeProvider timeProvider;

    public void publishRoomUpsertToCurrentMembers(Long roomId, ChatRoomListEventReason reason) {
        publishRoomUpsertToUsers(roomId, chatRoomMemberRepository.findUserIdsByRoomId(roomId), reason);
    }

    public void publishRoomUpsertToUsers(Long roomId, Collection<Long> userIds, ChatRoomListEventReason reason) {
        Objects.requireNonNull(userIds, "userIds must not be null");

        if (userIds.isEmpty()) {
            return;
        }

        userIds.stream()
                .distinct()
                .forEach(userId -> publishRoomUpsertToUser(roomId, userId, reason));
    }

    public void publishRoomRemoveToUser(Long roomId, Long userId, ChatRoomListEventReason reason) {
        Objects.requireNonNull(userId, "userId must not be null");

        ChatRoomType roomType = chatRoomRepository.findRoomTypeById(roomId)
                .orElseThrow(ChatRoomNotFoundException::new);
        ChatRoomListEventPayload payload = ChatRoomListEventPayload.remove(
                reason,
                roomId,
                roomType,
                timeProvider.now()
        );
        sendToUser(userId, payload);
    }

    public void publishRoomMetaUpsertByPostId(Long postId) {
        affectedRoomIdsByPostId(postId)
                .forEach(roomId -> publishRoomUpsertToCurrentMembers(
                        roomId,
                        ChatRoomListEventReason.ROOM_META_UPDATED
                ));
    }

    public void publishGroupRoomMetaUpsertByJourneyId(Long journeyId) {
        chatRoomRepository.findActiveGroupRoomIdsByJourneyId(journeyId)
                .forEach(roomId -> publishRoomUpsertToCurrentMembers(
                        roomId,
                        ChatRoomListEventReason.ROOM_META_UPDATED
                ));
    }

    public void publishPrivateRoomMetaUpsertByProfileUserId(Long profileUserId) {
        Map<Long, List<Long>> targetUserIdsByRoomId = chatRoomRepository
                .findPrivateRoomUpdateTargetsByProfileUserId(profileUserId)
                .stream()
                .collect(Collectors.groupingBy(
                        ChatRoomUserTargetQueryResult::chatRoomId,
                        Collectors.mapping(ChatRoomUserTargetQueryResult::userId, Collectors.toList())
                ));

        targetUserIdsByRoomId.forEach((roomId, userIds) -> publishRoomUpsertToUsers(
                roomId,
                userIds,
                ChatRoomListEventReason.ROOM_META_UPDATED
        ));
    }

    public List<Long> findCurrentMemberUserIds(Long roomId) {
        return chatRoomMemberRepository.findUserIdsByRoomId(roomId);
    }

    private void publishRoomUpsertToUser(Long roomId, Long userId, ChatRoomListEventReason reason) {
        chatRoomListService.retrieveChatRoomItem(userId, roomId)
                .map(chatRoom -> toUpsertPayload(reason, chatRoom))
                .ifPresent(payload -> sendToUser(userId, payload));
    }

    private ChatRoomListEventPayload toUpsertPayload(
            ChatRoomListEventReason reason,
            ChatRoomListItemResponse chatRoom
    ) {
        return ChatRoomListEventPayload.upsert(reason, chatRoom, timeProvider.now());
    }

    private Stream<Long> affectedRoomIdsByPostId(Long postId) {
        return Stream.concat(
                chatRoomRepository.findActivePrivateRoomIdsByPostId(postId).stream(),
                chatRoomRepository.findActiveGroupRoomIdsByJourneyPostId(postId).stream()
        );
    }

    private void sendToUser(Long userId, ChatRoomListEventPayload payload) {
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE,
                payload
        );
    }
}
