package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpendStyle implements CodedEnum {
    SPLURGE(1, "과감한 지출"),
    SAVER(2, "알뜰한 소비");

    private final int code;
    private final String text;
}
