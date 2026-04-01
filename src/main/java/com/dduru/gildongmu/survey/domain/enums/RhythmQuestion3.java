package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion3 implements SurveyOptionSpec, AxisBinaryScore {
    ROUTE_TIME_SET(1, 0, "📅", "동선이랑 시간은 어느 정도 정해둬야 마음이 편해~"),
    ROUGH_LIST_ONLY(2, 2, "📍", "대충 가고 싶은 곳만 정해두고, 나머진 현장 느낌대로!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
