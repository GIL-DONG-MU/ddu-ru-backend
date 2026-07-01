package com.dduru.gildongmu.chat.event;

import com.dduru.gildongmu.chat.service.ChatOnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class ChatStompDisconnectListener {

    private final ChatOnlineStatusService chatOnlineStatusService;

    @EventListener
    public void handle(SessionDisconnectEvent event) {
        chatOnlineStatusService.disconnect(event.getSessionId());
    }
}
