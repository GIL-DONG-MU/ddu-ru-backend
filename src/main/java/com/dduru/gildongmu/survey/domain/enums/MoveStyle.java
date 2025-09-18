package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MoveStyle implements CodedEnum {
    WALK_BUS(1, "걷기/대중교통"),
    TAXI(2, "택시/렌터카");

    private final int code;
    private final String text;
}
