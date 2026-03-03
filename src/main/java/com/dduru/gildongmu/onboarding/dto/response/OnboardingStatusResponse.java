package com.dduru.gildongmu.onboarding.dto.response;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;

public record OnboardingStatusResponse(
        boolean isOnboardingCompleted,
        boolean isProfileCompleted,
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
