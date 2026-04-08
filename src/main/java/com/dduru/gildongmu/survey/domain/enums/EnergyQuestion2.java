package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion2 implements CodedEnum, AxisBinaryScore {
    BRUNCH_INSTEAD(1, 0),
    BREAKFAST_SPRINT(2, 2);

    private final int code;
    private final int axisScorePoints;
}
