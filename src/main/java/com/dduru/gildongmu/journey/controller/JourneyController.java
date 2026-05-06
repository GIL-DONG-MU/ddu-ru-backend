package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.service.JourneyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JourneyController implements JourneyApiDocs {

    private final JourneyQueryService journeyQueryService;

    @Override
    @GetMapping("/users/me/journeys")
    public ResponseEntity<ApiResult<JourneyMainListResponse>> retrieveMyJourneys(
            @CurrentUser Long userId
    ) {
        JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
