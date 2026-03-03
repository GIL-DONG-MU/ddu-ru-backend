package com.dduru.gildongmu.onboarding.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.onboarding.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/users/me/onboarding/status")
    public ResponseEntity<ApiResult<OnboardingStatusResponse>> getOnboardingStatus(
            @CurrentUser Long userId
    ){
        OnboardingStatusResponse response = onboardingService.getStatus(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
