package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.dto.response.SurveyQuestionListResponse;
import com.dduru.gildongmu.survey.dto.response.SurveyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Survey", description = "설문조사 API")
@SecurityRequirement(name = "JWT")
public interface SurveyApiDocs {

    @Operation(summary = "설문조사 제출", description = "11개 질문의 선택지를 제출하고 성향 점수 및 아바타를 매칭합니다.")
    @ApiResponse(responseCode = "201", description = "설문 제출 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND
    })
    ResponseEntity<ApiResult<SurveyResponse>> submitSurvey(@Parameter(hidden = true) Long userId, @Valid SurveyRequest request);

    @Operation(summary = "내 설문 결과 조회", description = "현재 사용자의 설문 결과 및 매칭된 아바타를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.SURVEY_RESULT_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_NOT_FOUND
    })
    ResponseEntity<ApiResult<SurveyResponse>> getMySurveyResult(@Parameter(hidden = true) Long userId);

    @Operation(summary = "설문 문항 리스트 조회", description = "설문조사 11개 문항과 선택지를 조회합니다. 인증 없이 접근 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE
    })
    ResponseEntity<ApiResult<SurveyQuestionListResponse>> getSurveyQuestions();
}
