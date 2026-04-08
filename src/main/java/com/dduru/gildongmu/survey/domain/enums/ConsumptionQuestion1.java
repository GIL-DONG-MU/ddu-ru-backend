package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion1 implements CodedEnum, AxisBinaryScore {
    ADJUST_BUDGET(1, 0),
    FLEX_OK(2, 2);

    private final int code;
    private final int axisScorePoints;
}
