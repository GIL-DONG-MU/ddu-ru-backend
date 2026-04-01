package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion1 implements SurveyOptionSpec, AxisBinaryScore {
    ADJUST_BUDGET(1, 0, "📊", "잠깐… 지출 조정 타임. 예산에 맞춰야지"),
    FLEX_OK(2, 2, "💳", "여행인데 이 정도 FLEX는 괜찮지~");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
