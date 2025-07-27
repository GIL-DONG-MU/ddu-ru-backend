package com.dduru.gildongmu.auth.enums;

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
    UNKNOWN("Unknown", "알 수 없음");

    private final String value;
    private final String description;

    public static AgeRange from(String ageRange) {
        if (ageRange == null || ageRange.trim().isEmpty()) {
            return UNKNOWN;
        }

        switch (ageRange) {
            case "10~14":
            case "15~19":
                return AGE_10s;
            case "20~29":
                return AGE_20s;
            case "30~39":
                return AGE_30s;
            case "40~49":
                return AGE_40s;
            case "50~59":
                return AGE_50s;
            case "60~69":
                return AGE_60s;
            default:
                return UNKNOWN;
        }
    }
}
