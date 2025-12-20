package com.dduru.gildongmu.profile.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeRange {
    AGE_10s("10s", "10대"),
    AGE_20s("20s", "20대"),
    AGE_30s("30s", "30대"),
    AGE_40s("40s", "40대"),
    AGE_50s("50s", "50대"),
    AGE_60s("60s", "60대"),
    UNKNOWN("Unknown", "상관없음");

    private final String value;
    private final String description;

    public static AgeRange from(String ageRange) {
        if (ageRange == null || ageRange.trim().isEmpty()) {
            return UNKNOWN;
        }

        try {
            return AgeRange.valueOf(ageRange.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
