package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.survey.dto.SurveyRequest;
import com.dduru.gildongmu.survey.dto.SurveyQuestionListResponse;
import com.dduru.gildongmu.survey.dto.SurveyResponse;
import com.dduru.gildongmu.survey.service.SurveyQuestionService;
import com.dduru.gildongmu.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/surveys")
public class SurveyController implements SurveyApiDocs {

    private final SurveyService surveyService;
    private final SurveyQuestionService surveyQuestionService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<SurveyResponse>> submitSurvey(
            @CurrentUser Long userId,
            @Valid @RequestBody SurveyRequest request
    ) {
        SurveyResponse response = surveyService.submitSurvey(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResult<SurveyResponse>> getMySurveyResult(@CurrentUser Long userId) {
        SurveyResponse response = surveyService.getMySurveyResult(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/questions")
    public ResponseEntity<ApiResult<SurveyQuestionListResponse>> getSurveyQuestions() {
        SurveyQuestionListResponse response = surveyQuestionService.getSurveyQuestions();
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
