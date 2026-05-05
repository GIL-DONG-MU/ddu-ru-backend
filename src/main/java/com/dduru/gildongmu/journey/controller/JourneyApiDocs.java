package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyDetailResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journeys", description = "나의 여정 API")
@SecurityRequirement(name = "JWT")
public interface JourneyApiDocs {

    @Operation(
            summary = "나의 여정 메인 목록 조회",
            description = "로그인 사용자가 속한 여행 워크스페이스 목록을 조회합니다. 응답은 진행 중인 여행과 종료된 여행을 분리해서 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<JourneyMainListResponse>> retrieveMyJourneys(
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 상세 조회",
            description = "로그인 사용자가 속한 나의 여정 워크스페이스 상세 정보를 조회합니다. active journey member만 접근할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_NOT_FOUND
    })
    ResponseEntity<ApiResult<JourneyDetailResponse>> retrieveMyJourneyDetail(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 기본 정보 수정",
            description = "active journey member가 나의 여정의 제목과 대표 사진을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_EMPTY_PATCH,
            ErrorCode.JOURNEY_INVALID_TITLE_LENGTH,
            ErrorCode.JOURNEY_INVALID_PHOTO_URL
    })
    ResponseEntity<ApiResult<JourneyUpdateResponse>> updateJourneyBasicInfo(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            JourneyUpdateRequest request
    );
}
