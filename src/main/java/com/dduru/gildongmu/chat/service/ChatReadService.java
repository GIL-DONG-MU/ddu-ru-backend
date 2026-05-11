package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.ChatReadRequest;
import com.dduru.gildongmu.chat.dto.response.ChatReadResponse;
import com.dduru.gildongmu.chat.dto.ws.ChatReadEventPayload;
import com.dduru.gildongmu.chat.exception.ChatAccessDeniedException;
import com.dduru.gildongmu.chat.exception.ChatMessageNotFoundException;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.chat.repository.ChatMessageRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TimeProvider timeProvider;

    @Transactional
    public ChatReadResponse read(Long userId, Long roomId, ChatReadRequest request) {
        ChatRoom room = chatRoomRepository.getByIdWithContextOrThrow(roomId);
        validateRoomReadable(room);

        ChatRoomMember currentMember = chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)
                .orElseThrow(ChatAccessDeniedException::new);
        validateGroupAccess(room, userId);

        ChatMessage message = chatMessageRepository.findByIdAndRoomId(request.lastReadMessageId(), roomId)
                .orElseThrow(ChatMessageNotFoundException::new);
        validateMessageVisible(message, currentMember);

        boolean updated = currentMember.readUpTo(message);
        Long reflectedLastReadMessageId = currentMember.getLastReadMessage().getId();

        if (updated) {
            publishReadEventAfterCommit(roomId, userId, reflectedLastReadMessageId);
        }

        return new ChatReadResponse(roomId, reflectedLastReadMessageId, updated);
    }

    private static void validateRoomReadable(ChatRoom room) {
        if (room.getStatus() == ChatRoomStatus.DELETED) {
            throw new ChatRoomNotFoundException();
        }
    }

    private void validateGroupAccess(ChatRoom room, Long userId) {
        if (room.getRoomType() != ChatRoomType.GROUP) {
            return;
        }

        boolean activeJourneyMember = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                room.getJourney().getId(),
                userId,
                JourneyMemberStatus.ACTIVE
        );
        if (!activeJourneyMember) {
            throw new ChatAccessDeniedException();
        }
    }

    private static void validateMessageVisible(ChatMessage message, ChatRoomMember currentMember) {
        if (message.getCreatedAt().isBefore(currentMember.getCreatedAt())) {
            throw new ChatAccessDeniedException();
        }
    }

    private void publishReadEventAfterCommit(Long roomId, Long readerUserId, Long lastReadMessageId) {
        ChatReadEventPayload payload = ChatReadEventPayload.of(
                roomId,
                readerUserId,
                lastReadMessageId,
                timeProvider.now()
        );
        String destination = ChatDestinationPaths.topicRoom(roomId);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            simpMessagingTemplate.convertAndSend(destination, payload);
                        }
                    });
        } else {
            simpMessagingTemplate.convertAndSend(destination, payload);
        }
    }
}
