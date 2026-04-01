package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion2 implements SurveyOptionSpec, AxisBinaryScore {
    VALUE_TRANSPORT(1, 0, "🚇", "여행은 교통도 체험이지~ 가성비 이동!"),
    SAVE_TIME_TAXI(2, 2, "🚕", "시간 아끼는 게 곧 돈이야. 바로 택시!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
