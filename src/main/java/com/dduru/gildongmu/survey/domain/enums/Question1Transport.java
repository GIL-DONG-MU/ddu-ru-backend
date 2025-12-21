package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question1Transport implements CodedEnum {
    WALK_BUS(1, "걷기/버스"),
    TAXI(2, "택시");

    private final int code;
    private final String text;
}
