package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
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

    @Operation(summary = "참여 신청", description = "게시글에 참여 신청을 합니다.")
    @ApiResponse(responseCode = "201", description = "참여 신청 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.RECRUITMENT_CLOSED,
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

    @Operation(summary = "참여 신청 연락 시작", description = "방장이 신청자와 1:1 채팅을 시작하고 상태를 연락중으로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "연락 시작 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.PARTICIPATION_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.PARTICIPATION_POST_MISMATCH,
            ErrorCode.RECRUITMENT_CLOSED,
            ErrorCode.INVALID_PARTICIPATION_STATUS,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ParticipationContactResponse>> contactParticipation(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "동행 참여 신청 ID") Long participationId
    );

    @Operation(summary = "게시글 신청자 목록 조회", description = "해당 게시글에 대해 들어온 신청 목록을 최신순으로 조회합니다. 대기, 연락중, 승인, 거절 상태가 모두 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "신청자 목록 조회 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<List<ParticipationResponse>>> getPostParticipants(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId
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
