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

    @Operation(summary = "참여 신청 연락 시작", description = "방장이 신청자와 1:1 채팅을 시작하고 상태를 연락중으로 변경합니다. 정원이 가득 찬 게시글은 더 이상 신청을 처리할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "연락 시작 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.PARTICIPATION_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.PARTICIPATION_POST_MISMATCH,
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

    @Operation(summary = "참여 신청 승인", description = "대기 또는 연락중 상태의 게시글 참여 신청을 승인합니다. 정원이 가득 찬 게시글은 더 이상 신청을 승인할 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "참여 신청 승인 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.PARTICIPATION_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.RECRUITMENT_CLOSED,
            ErrorCode.RECRUITMENT_FULL,
            ErrorCode.RECRUIT_COUNT_EXCEED_CAPACITY,
            ErrorCode.CHAT_ROOM_NOT_FOUND,
            ErrorCode.INVALID_PARTICIPATION_STATUS,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ParticipationApproveResponse>> approveParticipation(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "동행 참여 신청 ID") Long participationId
    );

    @Operation(summary = "내가 받은 참여 신청 목록 조회", description = "내가 작성한 전체 게시글 기준으로 받은 동행 신청 목록을 최신순으로 조회합니다. `status`로 상태 필터링이 가능합니다.")
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
