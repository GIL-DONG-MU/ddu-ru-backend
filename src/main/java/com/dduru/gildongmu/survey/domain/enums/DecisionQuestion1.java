package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DecisionQuestion1 implements CodedEnum, AxisBinaryScore {
    DELEGATE_ROLE(1, 0),
    LEAD_OR_ORGANIZE(2, 2);

    private final int code;
    private final int axisScorePoints;
}
