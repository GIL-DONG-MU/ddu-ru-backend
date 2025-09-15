package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.common.enums.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TastingStyle implements CodedEnum {
    WAIT(1, "기다려서라도"),
    NEARBY(2, "주변에서 편하게");

    private final int code;
    private final String text;

    public static Optional<TastingStyle> fromCode(int code) {
        return EnumUtils.findByCode(values(), code);
    }
}
