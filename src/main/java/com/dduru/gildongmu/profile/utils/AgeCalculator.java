package com.dduru.gildongmu.profile.utils;

import java.time.LocalDate;
import java.time.Period;

public final class AgeCalculator {

    private AgeCalculator() {
    }

    public static Integer calculate(LocalDate birthday, LocalDate today) {
        if (birthday == null) {
            return null;
        }
        return Math.max(0, Period.between(birthday, today).getYears());
    }
}
