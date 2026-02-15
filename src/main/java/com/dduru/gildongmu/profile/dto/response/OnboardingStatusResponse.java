package com.dduru.gildongmu.profile.dto.response;

public record OnboardingStatusResponse(
        boolean isSignUpCompleted,
        boolean isBasicInfoCompleted,
        boolean isSurveyCompleted,
        boolean isSurveySkipped,
        boolean isProfileCompleted
) {
}
