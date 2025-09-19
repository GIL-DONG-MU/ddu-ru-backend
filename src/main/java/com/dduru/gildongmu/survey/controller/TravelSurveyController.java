package com.dduru.gildongmu.survey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.survey.dto.TravelSurveyRequest;
import com.dduru.gildongmu.survey.service.TravelSurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/travel-preferences")
public class TravelSurveyController implements TravelSurveyApiDocs{

    private final TravelSurveyService travelSurveyService;

    @Override
    @PostMapping
    public ResponseEntity<Void> submitTravelSurvey(
            @CurrentUser Long userId,
            @RequestBody @Valid TravelSurveyRequest request) {
        travelSurveyService.create(userId, request);
        return ResponseEntity.ok().build();
    }
}
