package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationApproveResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Participations", description = "동행 게시글 참여 관리 API")
@SecurityRequirement(name = "JWT")
public interface ParticipationApiDocs {

    @Operation(summary = "참여 신청", description = "게시글에 참여 신청을 합니다. 모집이 마감되었거나 정원이 가득 찬 게시글에는 신청할 수 없습니다.")
    @ApiResponse(responseCode = "201", description = "참여 신청 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.RECRUITMENT_CLOSED,
            ErrorCode.RECRUITMENT_FULL,
            ErrorCode.DUPLICATE_PARTICIPATION,
            ErrorCode.SELF_PARTICIPATION_NOT_ALLOWED,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ParticipationCreateResponse>> createParticipation(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Valid ParticipationRequest request
    );

    @Operation(
            summary = "참여 신청 연락 시작",
            description = "방장이 신청자와 1:1 채팅을 시작합니다. `PENDING`이면 `CONTACTING`으로 변경하고, 이미 `CONTACTING`이면 상태를 유지한 채 기존 채팅방을 재사용합니다."
    )
    @ApiResponse(responseCode = "200", description = "연락 시작 성공")
    @ApiErrorResponses({
            ErrorCode.PARTICIPATION_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.RECRUITMENT_CLOSED,
            ErrorCode.RECRUITMENT_FULL,
            ErrorCode.INVALID_PARTICIPATION_STATUS,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ParticipationContactResponse>> contactParticipation(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "동행 참여 신청 ID") Long participationId
    );

    @Operation(
            summary = "참여 신청 승인",
            description = "방장이 신청자를 승인합니다. `PENDING` 또는 `CONTACTING` 상태에서 승인 가능하며, 그룹 채팅방에 초대된 뒤 상태가 `APPROVED`로 변경됩니다. 이미 그룹 채팅방 멤버면 기존 방 정보를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "참여 신청 승인 성공")
    @ApiErrorResponses({
            ErrorCode.PARTICIPATION_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.RECRUITMENT_CLOSED,
            ErrorCode.RECRUITMENT_FULL,
            ErrorCode.INVALID_PARTICIPATION_STATUS,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.CHAT_ROOM_NOT_FOUND,
            ErrorCode.CHAT_ROOM_CLOSED,
            ErrorCode.CHAT_ROOM_CAPACITY_EXCEEDED,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ParticipationApproveResponse>> approveParticipation(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "동행 참여 신청 ID") Long participationId
    );

    @Operation(summary = "내가 받은 참여 신청 목록 조회", description = "내가 작성한 전체 게시글 기준으로 받은 동행 신청 목록을 최신순으로 조회합니다. `status` query parameter로 상태 필터링이 가능합니다.")
    @ApiResponse(responseCode = "200", description = "신청자 목록 조회 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<List<ParticipationRetrieveResponse>>> getParticipants(
            @Parameter(hidden = true) Long userId,
            @Valid @ParameterObject ParticipationRetrieveRequest request
    );
}
