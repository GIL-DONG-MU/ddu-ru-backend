package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.annotation.CommonApiResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorResponse;
import com.dduru.gildongmu.survey.dto.SurveyRequest;
import com.dduru.gildongmu.survey.dto.SurveyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Survey", description = "설문조사 API")
@SecurityRequirement(name = "JWT")
public interface SurveyApiDocs {

    @Operation(summary = "설문조사 제출", description = "11개 질문의 선택지를 제출하고 성향 점수 및 아바타를 매칭합니다.")
    @CommonApiResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "설문 제출 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SurveyResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 답변 코드, 필수 질문 미응답, Q7 범위 초과 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidAnswerCode",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "data": {
                                                        "errorCode": "INVALID_INPUT_VALUE",
                                                        "field": null,
                                                        "message": "Q1 이동수단에 대한 유효하지 않은 답변 코드입니다: 99"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ValidationError",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "data": {
                                                        "errorCode": "INVALID_INPUT_VALUE",
                                                        "field": "q1",
                                                        "message": "Q1 이동수단은 필수입니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidQ7Size",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "data": {
                                                        "errorCode": "INVALID_INPUT_VALUE",
                                                        "field": "q7",
                                                        "message": "Q7 선호활동은 최대 3개까지 선택할 수 있습니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ApiResult<SurveyResponse>> submitSurvey(
            @Parameter(hidden = true) Long userId,
            @Valid SurveyRequest request
    );

    @Operation(summary = "내 설문 결과 조회", description = "현재 사용자의 설문 결과 및 매칭된 아바타를 조회합니다.")
    @CommonApiResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SurveyResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "설문 결과를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "SurveyResultNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "data": {
                                                "errorCode": "SURVEY_RESULT_NOT_FOUND",
                                                "field": null,
                                                "message": "설문 결과를 찾을 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<SurveyResponse>> getMySurveyResult(
            @Parameter(hidden = true) Long userId
    );
}
