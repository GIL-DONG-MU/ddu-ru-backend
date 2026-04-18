package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.dto.ws.ChatMessageBroadcastPayload;
import com.dduru.gildongmu.chat.dto.ws.ChatMessageSendRequest;
import com.dduru.gildongmu.chat.exception.ChatAccessDeniedException;
import com.dduru.gildongmu.chat.exception.ChatRoomClosedException;
import com.dduru.gildongmu.chat.exception.ChatSystemMessageSendAccessDeniedException;
import com.dduru.gildongmu.chat.repository.ChatMessageRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.chat.validation.ChatImageUrlValidator;
import com.dduru.gildongmu.chat.validation.ChatTextValidator;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageSendService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    private final GroupChatRoomService groupChatRoomService;
    private final ProfileImageResolver profileImageResolver;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendUserMessage(Long senderUserId, Long roomId, ChatMessageSendRequest request) {
        ChatRoom room = chatRoomRepository.getByIdOrThrow(roomId);
        validateRoomOpen(room);
        checkSenderIsMember(senderUserId, roomId);

        User sender = userRepository.getByIdOrThrow(senderUserId);
        String content = resolveUserMessageContent(request);
        saveUserMessageAndMaybeActivate(room, sender, request.messageType(), content);
    }

    private void checkSenderIsMember(Long senderUserId, Long roomId) {
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(roomId, senderUserId)) {
            throw new ChatAccessDeniedException();
        }
    }

    private void saveUserMessageAndMaybeActivate(ChatRoom room, User sender, ChatMessageType messageType, String content) {
        long beforeCount = chatMessageRepository.countByRoom_Id(room.getId());
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(sender)
                .messageType(messageType)
                .content(content)
                .build());
        chatMessageRepository.flush();
        if (beforeCount == 0) {
            groupChatRoomService.activateChatOnFirstMessage(room);
        }
        scheduleBroadcastAfterCommit(toPayload(saved), room.getId());
    }

    private ChatMessageBroadcastPayload toPayload(ChatMessage saved) {
        Post post = saved.getRoom().getPost();

        return new ChatMessageBroadcastPayload(
                saved.getId(),
                saved.getRoom().getId(),
                post.getTitle(),
                post.getRecruitCount(),
                post.getRecruitCapacity(),
                saved.getMessageType(),
                null,
                saved.getCreatedAt()
        );
    }

    private static String resolveUserMessageContent(ChatMessageSendRequest request) {
        return switch (request.messageType()) {
            case TEXT -> ChatTextValidator.validateAndNormalize(request.content());
            case IMAGE -> ChatImageUrlValidator.validateAndNormalize(request.content());
            case SYSTEM -> throw new ChatSystemMessageSendAccessDeniedException();
        };
    }

    private static void validateRoomOpen(ChatRoom room) {
        if (room.getStatus() == ChatRoomStatus.CLOSED || room.getStatus() == ChatRoomStatus.DELETED) {
            throw new ChatRoomClosedException();
        }
    }

    /**
     * 트랜잭션이 커밋된 뒤에만 브로드캐스트해서, 롤백 시 잘못된 실시간 메시지가 나가지 않게 한다.
     * 테스트 등 비트랜잭션 호출에서는 즉시 전송한다.
     */
    private void scheduleBroadcastAfterCommit(ChatMessageBroadcastPayload payload, Long roomId) {
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
