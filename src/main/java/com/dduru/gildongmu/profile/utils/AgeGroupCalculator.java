package com.dduru.gildongmu.profile.utils;

import java.time.LocalDate;
import java.time.Period;

public class AgeGroupCalculator {

    private AgeGroupCalculator() {}

    public static Integer toAgeGroup(LocalDate birthday, LocalDate today) {
        if (birthday == null) return null;
        int age = Math.max(0, Period.between(birthday, today).getYears());
        return (age / 10) * 10;
    }
}
