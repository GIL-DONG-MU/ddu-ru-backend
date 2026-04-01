package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion1 implements SurveyOptionSpec, AxisBinaryScore {
    RELAXED_DAY(1, 0, "🐌", "카페-산책 … 여유롭게 즐기자 ~"),
    PACKED_DAY(2, 2, "🌅", "하루에 최대한 많이 다보자! 지금 아니면 언제 오겠어?");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
