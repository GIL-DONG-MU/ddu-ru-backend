package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journey Schedules", description = "나의 여정 일정 API")
@SecurityRequirement(name = "JWT")
public interface JourneyScheduleApiDocs {

    @Operation(
            summary = "나의 여정 일정 목록 조회",
            description = "active journey member가 여행 기간 내 일정을 Day 단위로 묶어 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<JourneyScheduleListResponse>> retrieveSchedules(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 일정 생성",
            description = "active journey member가 여행 기간 내 날짜에 일정을 추가합니다."
    )
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_TITLE,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_MEMO,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_PLACE_NAME,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME,
            ErrorCode.IMAGE_URL_NOT_ALLOWED
    })
    ResponseEntity<ApiResult<JourneyScheduleListResponse>> createSchedule(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            JourneyScheduleCreateRequest request
    );

    @Operation(
            summary = "나의 여정 일정 수정",
            description = "active journey member가 일정을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_SCHEDULE_NOT_FOUND,
            ErrorCode.JOURNEY_SCHEDULE_EMPTY_PATCH,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_TITLE,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_MEMO,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_PLACE_NAME,
            ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME,
            ErrorCode.IMAGE_URL_NOT_ALLOWED
    })
    ResponseEntity<ApiResult<JourneyScheduleListResponse>> updateSchedule(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "일정 ID") Long scheduleId,
            @Parameter(hidden = true) Long userId,
            JourneyScheduleUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 일정 삭제",
            description = "active journey member가 일정을 soft delete 처리합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_SCHEDULE_NOT_FOUND
    })
    ResponseEntity<ApiResult<Void>> deleteSchedule(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "일정 ID") Long scheduleId,
            @Parameter(hidden = true) Long userId
    );
}
