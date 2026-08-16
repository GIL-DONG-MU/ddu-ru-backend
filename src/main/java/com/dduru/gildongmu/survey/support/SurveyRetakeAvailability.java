package com.dduru.gildongmu.survey.support;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record SurveyRetakeAvailability(
        boolean canRetake,
        LocalDate nextRetakeAvailableDate,
        long remainingRetakeDays
) {
    public static SurveyRetakeAvailability of(LocalDate lastTestedAt, LocalDate today, int cooldownDays) {
        LocalDate nextRetakeAvailableDate = lastTestedAt.plusDays(cooldownDays);
        boolean canRetake = !today.isBefore(nextRetakeAvailableDate);
        long remainingRetakeDays = canRetake ? 0 : ChronoUnit.DAYS.between(today, nextRetakeAvailableDate);
        return new SurveyRetakeAvailability(canRetake, nextRetakeAvailableDate, remainingRetakeDays);
    }
}
