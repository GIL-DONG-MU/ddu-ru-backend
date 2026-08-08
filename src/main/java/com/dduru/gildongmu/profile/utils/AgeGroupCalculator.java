package com.dduru.gildongmu.profile.utils;

import java.time.LocalDate;

public class AgeGroupCalculator {

    private AgeGroupCalculator() {}

    public static String toAgeGroup(LocalDate birthday, LocalDate today) {
        Integer age = AgeCalculator.calculate(birthday, today);
        if (age == null) return null;
        return (age / 10) * 10 + "대";
    }
}
