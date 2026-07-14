package com.dduru.gildongmu.recommendation.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Mate Recommendation", description = "여행방 추천 API")
public interface MateRecommendationApiDocs {

    @Operation(summary = "추천 여행방 패스", description = "본인에게 추천된 여행방을 패스하고 이후 추천 후보에서 제외합니다.")
    @ApiResponse(responseCode = "204", description = "패스 처리 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.MATE_RECOMMENDATION_NOT_FOUND
    })
    ResponseEntity<ApiResult<Void>> pass(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "추천 결과 ID", example = "1") Long recommendationId
    );
}
