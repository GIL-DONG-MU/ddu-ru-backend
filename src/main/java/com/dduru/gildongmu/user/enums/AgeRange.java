package com.dduru.gildongmu.user.enums;

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

        return switch (ageRange) {
            case "10~14", "15~19" -> AGE_10s;
            case "20~29" -> AGE_20s;
            case "30~39" -> AGE_30s;
            case "40~49" -> AGE_40s;
            case "50~59" -> AGE_50s;
            case "60~69" -> AGE_60s;
            default -> {
                for (AgeRange range : values()) {
                    if (range.name().equalsIgnoreCase(ageRange) ||
                            range.value.equalsIgnoreCase(ageRange)) {
                        yield range;
                    }
                }
                yield UNKNOWN;
            }
        };
    }
}
