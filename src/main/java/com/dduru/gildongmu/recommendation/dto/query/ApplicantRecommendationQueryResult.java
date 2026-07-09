package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicantRecommendationQueryResult(
        SurveyStatus surveyStatus,
        Long profileId,
        Gender gender,
        LocalDate birthday,
        Long travelTendencyId,
        BigDecimal rhythmScore,
        BigDecimal energyScore,
        BigDecimal consumptionScore,
        BigDecimal decisionScore
) {
    public boolean isSurveyCompleted() {
        return surveyStatus == SurveyStatus.COMPLETED;
    }

    public boolean hasProfile() {
        return profileId != null;
    }

    public boolean hasTravelTendency() {
        return travelTendencyId != null;
    }
}
