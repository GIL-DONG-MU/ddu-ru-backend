package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.survey.dto.TravelSurveyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "TravelSurveyApi", description = "여행 취향 테스트 API")
@SecurityRequirement(name = "JWT")
public interface TravelSurveyApiDocs {

    @Operation(summary = "여행 취향 설문 저장", description = "사용자의 여행 취향 설문을 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "테스트 저장 성공"),
    })
    ResponseEntity<ApiResult<Void>> submitTravelSurvey(
            @Parameter(hidden = true) Long userId,
            @Valid TravelSurveyRequest request
    );
}
