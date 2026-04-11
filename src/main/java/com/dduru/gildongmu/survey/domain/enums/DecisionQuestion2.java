package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion2 implements CodedEnum, AxisBinaryScore {
    FOLLOW_OTHERS(1, 0),
    PROPOSE_FIRST(2, 2);

    private final int code;
    private final int axisScorePoints;
}
