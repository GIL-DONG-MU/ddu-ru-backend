package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StayStyle implements CodedEnum {
    HOTEL(1, "호텔"),
    JUST_SLEEP(2, "잠만 자기");

    private final int code;
    private final String text;
}
