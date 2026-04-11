package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion3 implements CodedEnum, AxisBinaryScore {
    DO_NOTHING_OK(1, 0),
    FILL_WITH_SPOTS(2, 2);

    private final int code;
    private final int axisScorePoints;
}
