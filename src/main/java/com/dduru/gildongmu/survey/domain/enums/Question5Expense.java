package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question5Expense implements CodedEnum {
    EACH_PAYS(1, "각자 결제"),
    POOLED(2, "모아 쓰기");

    private final int code;
    private final String text;
}
