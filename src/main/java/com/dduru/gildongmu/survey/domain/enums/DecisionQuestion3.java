package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion3 implements CodedEnum, AxisBinaryScore {
    WAIT_AND_SEE(1, 0),
    DRIVE_CONCLUSION(2, 2);

    private final int code;
    private final int axisScorePoints;
}
