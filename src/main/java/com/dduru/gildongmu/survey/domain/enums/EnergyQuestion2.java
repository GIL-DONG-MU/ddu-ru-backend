package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion2 implements SurveyOptionSpec, AxisBinaryScore {
    BRUNCH_INSTEAD(1, 0, "🙅🏻‍♀️", "오픈런은 무리… 느지막이 일어나서 근처 브런치 카페나 갈까?"),
    BREAKFAST_SPRINT(2, 2, "🏃", "1등으로 입장! 조식 빨리 먹고 남들보다 먼저 관광지로 출발~");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
