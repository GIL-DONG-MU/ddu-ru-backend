package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.ws.ChatMessageSendRequest;
import com.dduru.gildongmu.chat.service.ChatMessageSendService;
import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageSendService chatMessageSendService;

    @MessageMapping(ChatDestinationPaths.PUB_ROOM_MESSAGES_PATTERN)
    public void sendMessage(
            Principal principal,
            @DestinationVariable Long roomId,
            @Valid @Payload ChatMessageSendRequest request
    ) {
        Long userId = Long.parseLong(principal.getName());
        chatMessageSendService.sendUserMessage(userId, roomId, request);
    }
}
