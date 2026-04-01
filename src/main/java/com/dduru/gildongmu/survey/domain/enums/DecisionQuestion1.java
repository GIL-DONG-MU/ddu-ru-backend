package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion1 implements SurveyOptionSpec, AxisBinaryScore {
    DELEGATE_ROLE(1, 0, "🙂", "나는 맡겨주는 역할 하는 게 편해"),
    LEAD_OR_ORGANIZE(2, 2, "📋", "자연스럽게 총괄 또는 정리하는 편!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
