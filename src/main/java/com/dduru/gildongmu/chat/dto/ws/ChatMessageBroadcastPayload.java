package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.system.ChatSystemMessageFactory;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 채팅방 토픽 구독자에게 브로드캐스트되는 메시지 페이로드.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ChatMessageBroadcastPayload(
        Long messageId,
        Long roomId,
        String postTitle,
        int recruitCount,
        int recruitCapacity,
        ChatMessageType messageType,
        ChatMessageSenderPayload sender,
        ChatUserMessagePayload userMessage,
        ChatSystemMessagePayload systemMessage,
        LocalDateTime createdAt
) {
    public static ChatMessageBroadcastPayload ofUserMessage(ChatMessage message, Post post, User sender, ProfileImageResolver profileImageResolver) {
        return ChatMessageBroadcastPayload.builder()
                .messageId(message.getId())
                .roomId(message.getRoom().getId())
                .postTitle(post.getTitle())
                .recruitCount(post.getRecruitCount())
                .recruitCapacity(post.getRecruitCapacity())
                .messageType(message.getMessageType())
                .sender(ChatMessageSenderPayload.from(sender, post.getUser().getId(), profileImageResolver))
                .userMessage(ChatUserMessagePayload.from(message.getMessageType(), message.getContent()))
                .systemMessage(null)
                .createdAt(message.getCreatedAt())
                .build();

    }

    public static ChatMessageBroadcastPayload ofSystemMessage(ChatMessage message, Post post, ChatSystemMessageFactory chatSystemMessageFactory) {
        return ChatMessageBroadcastPayload.builder()
                .messageId(message.getId())
                .roomId(message.getRoom().getId())
                .postTitle(post.getTitle())
                .recruitCount(post.getRecruitCount())
                .recruitCapacity(post.getRecruitCapacity())
                .messageType(message.getMessageType())
                .sender(null)
                .userMessage(null)
                .systemMessage(chatSystemMessageFactory.deserialize(message.getContent()))
                .createdAt(message.getCreatedAt())
                .build();
    }

}
