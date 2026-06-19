package com.dduru.gildongmu.chat.event;

import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventReason;
import com.dduru.gildongmu.chat.service.ChatRoomListRealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 채팅, 게시글, 여정, 프로필 도메인 이벤트를 채팅방 목록 실시간(WebSocket) 이벤트로 변환한다.
 * <p>
 * 모든 핸들러는 원 트랜잭션 커밋 이후에 실행되어, 롤백된 변경이 사용자 개인 목록 queue로 발행되지 않도록 한다.
 */
@Component
@RequiredArgsConstructor
public class ChatRoomListRealtimeEventListener {

    private final ChatRoomListRealtimePublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageCreatedEvent event) {
        publisher.publishRoomUpsertToCurrentMembers(event.roomId(), ChatRoomListEventReason.MESSAGE_CREATED);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatReadUpdatedEvent event) {
        publisher.publishRoomUpsertToUsers(
                event.roomId(),
                List.of(event.readerUserId()),
                ChatRoomListEventReason.READ_UPDATED
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMemberChangedEvent event) {
        if (event.changeType() == ChatMemberChangeType.MEMBER_ADDED) {
            publisher.publishRoomUpsertToCurrentMembers(event.roomId(), ChatRoomListEventReason.MEMBER_CHANGED);
            return;
        }

        // 제거일 경우, 나머지 멤버들에게는 upsert, 탈퇴하는 멤버에게는 remove 이벤트를 보낸다.
        publisher.publishRoomUpsertToUsers(
                event.roomId(),
                publisher.findCurrentMemberUserIds(event.roomId()),
                ChatRoomListEventReason.MEMBER_CHANGED
        );
        publisher.publishRoomRemoveToUser(
                event.roomId(),
                event.memberUserId(),
                ChatRoomListEventReason.MEMBER_CHANGED
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostUpdatedEvent event) {
        publisher.publishRoomMetaUpsertByPostId(event.postId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(JourneyBasicInfoUpdatedEvent event) {
        publisher.publishGroupRoomMetaUpsertByJourneyId(event.journeyId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProfileUpdatedEvent event) {
        publisher.publishPrivateRoomMetaUpsertByProfileUserId(event.userId());
    }
}
