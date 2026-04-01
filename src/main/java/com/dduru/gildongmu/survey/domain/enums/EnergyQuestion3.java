package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion3 implements SurveyOptionSpec, AxisBinaryScore {
    DO_NOTHING_OK(1, 0, "🌿", "아무것도 안 하는 날도 필요해"),
    FILL_WITH_SPOTS(2, 2, "📍", "근처 명소 검색해서 바로 채우기!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
