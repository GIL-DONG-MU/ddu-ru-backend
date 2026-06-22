package com.dduru.gildongmu.notification.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.notification.dto.request.NotificationListRequest;
import com.dduru.gildongmu.notification.dto.response.NotificationListResponse;
import com.dduru.gildongmu.notification.dto.response.NotificationReadResponse;
import com.dduru.gildongmu.notification.dto.response.UnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Notifications", description = "알림 API")
@SecurityRequirement(name = "JWT")
public interface NotificationApiDocs {

    @Operation(
            summary = "알림 목록 조회",
            description = "본인에게 도착한 알림을 최신순(커서 페이지네이션)으로 조회합니다. cursor 미전달 시 최신부터 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<NotificationListResponse>> getNotifications(
            @Parameter(hidden = true) Long userId,
            @Valid @ParameterObject NotificationListRequest request
    );

    @Operation(summary = "안 읽은 알림 개수 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<UnreadCountResponse>> getUnreadCount(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "단건 읽음 처리", description = "본인에게 도착한 알림 1건을 읽음 처리합니다. 이미 읽은 알림은 멱등 처리됩니다.")
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.NOTIFICATION_NOT_FOUND,
            ErrorCode.NOTIFICATION_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<NotificationReadResponse>> markAsRead(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "알림 ID", required = true) @PathVariable Long notificationId
    );

    @Operation(summary = "전체 읽음 처리", description = "본인의 모든 미읽음 알림을 일괄 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<NotificationReadResponse>> markAllAsRead(
            @Parameter(hidden = true) Long userId
    );
}
