package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion3 implements SurveyOptionSpec, AxisBinaryScore {
    WAIT_AND_SEE(1, 0, "😅", "나도 일단 말 안 하고 흐름 타기"),
    DRIVE_CONCLUSION(2, 2, "🚀", "답답해! 내가 정리해서 결론 낸다");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
