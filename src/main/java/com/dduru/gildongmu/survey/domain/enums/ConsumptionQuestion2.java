package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsumptionQuestion2 implements CodedEnum, AxisBinaryScore {
    VALUE_TRANSPORT(1, 0),
    SAVE_TIME_TAXI(2, 2);

    private final int code;
    private final int axisScorePoints;
}
