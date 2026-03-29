package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Chat", description = "채팅 API")
public interface ChatApiDocs {

    @Operation(summary = "1:1 채팅방 생성/조회", description = "게시글 단위로 1:1 채팅방을 생성하거나 기존 방을 조회합니다.")
    @ApiResponse(responseCode = "201", description = "신규 생성")
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
            summary = "그룹 채팅 멤버 초대",
            description = "방장이 기존 그룹 채팅방에 사용자를 1명 이상 초대합니다. 이미 참여 중인 사용자는 건너뜁니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.CHAT_ROOM_NOT_FOUND,
            ErrorCode.CHAT_ROOM_CLOSED,
            ErrorCode.CHAT_ROOM_CAPACITY_EXCEEDED,
            ErrorCode.CHAT_ROOM_INVITE_ACCESS_DENIED,
            ErrorCode.INVALID_INPUT_VALUE
    })
    ResponseEntity<ApiResult<GroupChatInviteResponse>> inviteMembersToGroupRoom(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long roomId,
            @Valid GroupChatInviteRequest request
    );
}
