package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.request.GroupChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.request.PrivateChatRoomCreateRequest;
import com.dduru.gildongmu.chat.dto.response.ChatRoomCreateResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Chat", description = "채팅 API")
public interface ChatApiDocs {

    @Operation(summary = "1:1 채팅방 생성/조회", description = "게시글 단위로 1:1 채팅방을 생성하거나 기존 방을 조회합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.NOT_SELF_CHAT
    })
    ResponseEntity<ApiResult<ChatRoomCreateResponse>> createPrivateRoom(
            @Parameter(hidden = true) Long userId,
            @Valid PrivateChatRoomCreateRequest request
    );

    @Operation(summary = "그룹 채팅방 생성", description = "그룹(1:N) 채팅방을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.CHAT_ROOM_CAPACITY_EXCEEDED
    })
    ResponseEntity<ApiResult<ChatRoomCreateResponse>> createGroupRoom(
            @Parameter(hidden = true) Long userId,
            @Valid GroupChatRoomCreateRequest request
    );
}
