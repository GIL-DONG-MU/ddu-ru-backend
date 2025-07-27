package com.dduru.gildongmu.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    AGE_10s("10s", "10대"),
    AGE_20s("20s", "20대"),
    AGE_30s("30s", "30대"),
    AGE_40s("40s", "40대"),
    AGE_50s("50s", "50대"),
    AGE_60s("60s", "60대"),
    UNKNOWN("Unknown", "알 수 없음");

    private final String value;
    private final String description;
}
