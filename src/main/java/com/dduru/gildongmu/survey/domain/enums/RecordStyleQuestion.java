package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStyleQuestion implements CodedEnum {
    EYES_FIRST(1),
    SHOOT_NOW(2);

    private final int code;
}
