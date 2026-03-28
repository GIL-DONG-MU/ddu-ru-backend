package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.request.GroupChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.request.PrivateChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.response.ChatRoomCreateResponse;
import com.dduru.gildongmu.chat.service.ChatRoomService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/rooms")
public class ChatController implements ChatApiDocs {

    private final ChatRoomService chatRoomService;

    @Override
    @PostMapping("/private")
    public ResponseEntity<ApiResult<ChatRoomCreateResponse>> createPrivateRoom(
            @CurrentUser Long userId,
            @Valid @RequestBody PrivateChatRoomCreateRequest request
    ) {
        ChatRoomCreateResponse response = chatRoomService.createOrGetPrivateRoom(userId, request);
        if (!response.isCreated()) {
            return ResponseEntity.ok(ApiResult.ok(response));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @PostMapping("/group")
    public ResponseEntity<ApiResult<ChatRoomCreateResponse>> createGroupRoom(
            @CurrentUser Long userId,
            @Valid @RequestBody GroupChatRoomCreateRequest request
    ) {
        ChatRoomCreateResponse response = chatRoomService.createGroupRoom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }
}
