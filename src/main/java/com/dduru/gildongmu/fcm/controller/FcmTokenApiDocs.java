package com.dduru.gildongmu.fcm.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenDeleteRequest;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "FCM Token", description = "FCM 토큰 관리 API")
@SecurityRequirement(name = "JWT")
public interface FcmTokenApiDocs {

    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = "기기의 FCM 토큰을 등록합니다. 동일 기기 타입으로 이미 토큰이 있으면 갱신합니다."
    )
    @ApiResponse(responseCode = "204", description = "등록/갱신 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED, ErrorCode.INVALID_INPUT_VALUE})
    ResponseEntity<ApiResult<Void>> register(
            @Parameter(hidden = true) Long userId,
            @Valid @RequestBody FcmTokenRegisterRequest request
    );

    @Operation(
            summary = "FCM 토큰 삭제",
            description = "로그아웃 시 해당 기기의 FCM 토큰을 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED, ErrorCode.INVALID_INPUT_VALUE})
    ResponseEntity<ApiResult<Void>> delete(
            @Parameter(hidden = true) Long userId,
            @Valid @RequestBody FcmTokenDeleteRequest request
    );
}
