package com.dduru.gildongmu.chat.websocket;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.service.ChatOnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatStompPresenceInterceptor implements ChannelInterceptor {

    private final ChatOnlineStatusService chatOnlineStatusService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;

        switch (accessor.getCommand()) {
            case SUBSCRIBE -> handleSubscribe(accessor);
            case UNSUBSCRIBE -> handleUnsubscribe(accessor);
            default -> { }
        }

        return message;
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        extractRoomId(accessor.getDestination()).ifPresent(roomId -> {
            Long userId = extractUserId(accessor);
            if (userId == null) return;
            chatOnlineStatusService.enter(roomId, userId, accessor.getSessionId(), accessor.getSubscriptionId());
        });
    }

    // UNSUBSCRIBE 프레임은 destination 없이 subscriptionId만 전달 → subscriptionId로 roomId 역추적
    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        Long userId = extractUserId(accessor);
        if (userId == null) return;
        String subscriptionId = accessor.getSubscriptionId();
        if (subscriptionId == null) return;
        chatOnlineStatusService.leave(accessor.getSessionId(), subscriptionId, userId);
    }

    private Optional<Long> extractRoomId(String destination) {
        if (destination == null || !destination.startsWith(ChatDestinationPaths.TOPIC_ROOM_PREFIX)) {
            return Optional.empty();
        }
        try {
            String suffix = destination.substring(ChatDestinationPaths.TOPIC_ROOM_PREFIX.length());
            return Optional.of(Long.parseLong(suffix));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Long extractUserId(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user == null) return null;
        try {
            return Long.parseLong(user.getName()); // JWT 인증 시 Principal.name = userId
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
