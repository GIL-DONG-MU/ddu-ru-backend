package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.common.enums.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ExpenseStyle implements CodedEnum {
    EACH_PAYS(1, "각자 계산"),
    POOLED(2, "통장 관리");

    private final int code;
    private final String text;

    public static Optional<ExpenseStyle> fromCode(int code) {
        return EnumUtils.findByCode(values(), code);
    }
}
