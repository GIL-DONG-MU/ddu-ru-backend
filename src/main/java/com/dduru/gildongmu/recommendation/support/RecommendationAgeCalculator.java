package com.dduru.gildongmu.recommendation.support;

import java.time.LocalDate;
import java.time.Period;

public final class RecommendationAgeCalculator {

    private RecommendationAgeCalculator() {
    }

    public static Integer calculate(LocalDate birthday, LocalDate today) {
        if (birthday == null) {
            return null;
        }
        return Math.max(0, Period.between(birthday, today).getYears());
    }
}
