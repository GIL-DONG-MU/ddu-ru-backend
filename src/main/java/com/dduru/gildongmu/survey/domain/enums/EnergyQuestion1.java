package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnergyQuestion1 implements CodedEnum, AxisBinaryScore {
    RELAXED_DAY(1, 0),
    PACKED_DAY(2, 2);

    private final int code;
    private final int axisScorePoints;
}
