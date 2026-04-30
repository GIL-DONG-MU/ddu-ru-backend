package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
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
            description = "로그인 사용자가 속한 여행 메인 카드 목록을 조회합니다. 응답은 진행 중인 여행과 종료된 여행을 분리해서 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<JourneyMainListResponse>> retrieveMyJourneys(
            @Parameter(hidden = true) Long userId
    );
}
