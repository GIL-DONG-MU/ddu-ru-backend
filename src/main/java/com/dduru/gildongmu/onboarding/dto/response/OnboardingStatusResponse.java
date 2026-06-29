package com.dduru.gildongmu.onboarding.dto.response;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record OnboardingStatusResponse(
        @Schema(description = "온보딩 전체 완료 여부", example = "false")
        boolean isOnboardingCompleted,
        @Schema(description = "기본 프로필 입력 완료 여부", example = "true")
        boolean isProfileCompleted,
        @Schema(description = "설문 진행 상태", example = "COMPLETED")
        SurveyStatus surveyStatus
) {
    public static OnboardingStatusResponse from(UserOnboarding userOnboarding) {
        return new OnboardingStatusResponse(
                userOnboarding.isOnboardingCompleted(),
                userOnboarding.isProfileCompleted(),
                userOnboarding.getSurveyStatus()
        );

    }
}
