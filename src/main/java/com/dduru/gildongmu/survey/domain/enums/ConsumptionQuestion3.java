package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion3 implements SurveyOptionSpec, AxisBinaryScore {
    VALUE_CHOICE(1, 0, "📊", "굳이 돈 안 써도 즐길 방법 많지! 가성비로 간다"),
    INVEST_EXPERIENCE(2, 2, "✨", "여행 추억은 이런 데서 생기지. 경험에 투자!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
