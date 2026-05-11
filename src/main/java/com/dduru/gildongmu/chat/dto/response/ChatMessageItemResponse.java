package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.dto.ws.ChatSystemMessagePayload;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ChatMessageItemResponse(
        Long messageId,
        ChatMessageType messageType,
        ChatMessageSenderResponse sender,
        boolean isMine,
        String content,
        List<ChatMessageImageResponse> images,
        ChatSystemMessageResponse systemMessage,
        Integer unreadCount,
        LocalDateTime createdAt
) {
    public static ChatMessageItemResponse forText(
            ChatMessage message,
            Long hostUserId,
            Long currentUserId,
            Integer unreadCount
    ) {
        return new ChatMessageItemResponse(
                message.getId(),
                message.getMessageType(),
                ChatMessageSenderResponse.from(message.getSender(), hostUserId),
                isMine(message.getSender(), currentUserId),
                message.getContent(),
                List.of(),
                null,
                unreadCount,
                message.getCreatedAt()
        );
    }

    public static ChatMessageItemResponse forImage(
            ChatMessage message,
            Long hostUserId,
            Long currentUserId,
            Integer unreadCount
    ) {
        return new ChatMessageItemResponse(
                message.getId(),
                message.getMessageType(),
                ChatMessageSenderResponse.from(message.getSender(), hostUserId),
                isMine(message.getSender(), currentUserId),
                null,
                List.of(ChatMessageImageResponse.from(message.getContent())),
                null,
                unreadCount,
                message.getCreatedAt()
        );
    }

    public static ChatMessageItemResponse forSystem(
            ChatMessage message,
            ChatSystemMessagePayload payload,
            Map<Long, User> systemUsers
    ) {
        return new ChatMessageItemResponse(
                message.getId(),
                message.getMessageType(),
                null,
                false,
                null,
                List.of(),
                ChatSystemMessageResponse.from(payload, systemUsers),
                null,
                message.getCreatedAt()
        );
    }

    private static boolean isMine(User sender, Long currentUserId) {
        return sender != null && sender.getId().equals(currentUserId);
    }
}
