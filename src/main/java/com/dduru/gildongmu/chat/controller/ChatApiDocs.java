package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.request.ChatMessageRetrieveRequest;
import com.dduru.gildongmu.chat.dto.request.ChatReadRequest;
import com.dduru.gildongmu.chat.dto.response.ChatMessagesResponse;
import com.dduru.gildongmu.chat.dto.response.ChatReadResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Chat", description = "채팅 API")
@SecurityRequirement(name = "JWT")
public interface ChatApiDocs {

    @Operation(summary = "1:1 채팅방 생성/조회", description = "게시글 단위로 1:1 채팅방을 생성(201)하거나 이미 있을 시 기존 방을 조회(200)해서 리턴합니다.")
    @ApiResponse(responseCode = "200", description = "기존 채팅방 조회")
    @ApiResponse(responseCode = "201", description = "신규 채팅방 생성")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.NOT_SELF_CHAT
    })
    ResponseEntity<ApiResult<PrivateChatRoomCreateResponse>> createPrivateRoom(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId
    );

    @Operation(
            summary = "채팅방 메시지 목록 조회",
            description = "채팅방의 메시지를 beforeMessageId 커서 기준으로 조회합니다. 응답 메시지는 오래된 순서로 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.CHAT_ROOM_NOT_FOUND,
            ErrorCode.CHAT_ACCESS_DENIED,
            ErrorCode.CHAT_MESSAGE_NOT_FOUND,
            ErrorCode.JSON_CONVERT_ERROR
    })
    ResponseEntity<ApiResult<ChatMessagesResponse>> retrieveMessages(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @Valid @ParameterObject ChatMessageRetrieveRequest request
    );

    @Operation(
            summary = "채팅방 메시지 읽음 처리",
            description = "현재 사용자의 채팅방 읽음 위치를 lastReadMessageId까지 갱신합니다. 기존 읽음 위치보다 같거나 과거인 경우 updated=false로 응답합니다."
    )
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.CHAT_ROOM_NOT_FOUND,
            ErrorCode.CHAT_MESSAGE_NOT_FOUND,
            ErrorCode.CHAT_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<ChatReadResponse>> readMessages(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @Valid @RequestBody ChatReadRequest request
    );
}
