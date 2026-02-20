package com.dduru.gildongmu.profile.dto.response;

import com.dduru.gildongmu.profile.domain.enums.SurveyStatus;

public record OnboardingStatusResponse(
        boolean onboardingCompleted,
        boolean profileCompleted,
        SurveyStatus surveyStatus
) {
}
