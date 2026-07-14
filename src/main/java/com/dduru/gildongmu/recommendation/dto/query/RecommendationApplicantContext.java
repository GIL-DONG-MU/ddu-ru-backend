package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.recommendation.support.AvailableDateRange;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;

import java.time.LocalDate;
import java.util.List;

public record RecommendationApplicantContext(
        LocalDate today,
        Gender gender,
        Integer age,
        DestinationPreferenceFilter destinationPreferenceFilter,
        List<AvailableDateRange> availableDateRanges,
        TravelTendencyScores applicantScores
) {
}
