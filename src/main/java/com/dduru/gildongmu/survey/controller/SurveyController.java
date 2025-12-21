package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.survey.dto.SurveyRequest;
import com.dduru.gildongmu.survey.dto.SurveyResponse;
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

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<SurveyResponse>> submitSurvey(
            @CurrentUser Long userId,
            @RequestBody @Valid SurveyRequest request) {
        SurveyResponse response = surveyService.submitSurvey(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResult<SurveyResponse>> getMySurveyResult(
            @CurrentUser Long userId) {
        SurveyResponse response = surveyService.getMySurveyResult(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(response));
    }
}
