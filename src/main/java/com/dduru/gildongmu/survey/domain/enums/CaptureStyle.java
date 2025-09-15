package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.common.enums.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum CaptureStyle implements CodedEnum {
    PHOTO(1, "사진으로 기록"),
    EYES(2, "눈으로 담기");

    private final int code;
    private final String text;

    public static Optional<CaptureStyle> fromCode(int code) {
        return EnumUtils.findByCode(values(), code);
    }
}
