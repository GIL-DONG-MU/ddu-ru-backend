package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.service.PrivateChatRoomService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController implements ChatApiDocs {

    private final PrivateChatRoomService privateChatRoomService;

    @Override
    @PostMapping("/posts/{postId}/private")
    public ResponseEntity<ApiResult<PrivateChatRoomCreateResponse>> createPrivateRoom(
            @CurrentUser Long userId,
            @PathVariable Long postId
    ) {
        PrivateChatRoomCreateResponse response = privateChatRoomService.createOrGetRoom(userId, postId);
        if (!response.isCreated()) {
            return ResponseEntity.ok(ApiResult.ok(response));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }
}
