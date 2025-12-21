package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question6Spend implements CodedEnum {
    SPLURGE(1, "쓴다 (플랙스)"),
    SAVE(2, "아낀다 (가성비)");

    private final int code;
    private final String text;
}
