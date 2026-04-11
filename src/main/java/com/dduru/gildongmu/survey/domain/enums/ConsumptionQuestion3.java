package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion3 implements CodedEnum, AxisBinaryScore {
    VALUE_CHOICE(1, 0),
    INVEST_EXPERIENCE(2, 2);

    private final int code;
    private final int axisScorePoints;
}
