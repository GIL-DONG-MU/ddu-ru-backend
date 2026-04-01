package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion2 implements SurveyOptionSpec, AxisBinaryScore {
    FOLLOW_OTHERS(1, 0, "👍", "난 다 좋아~ 따라갈게!"),
    PROPOSE_FIRST(2, 2, "🗣️", "내가 먼저 \"이렇게 해볼까?\" 하고 제안한다.");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
