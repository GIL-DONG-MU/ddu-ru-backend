package com.dduru.gildongmu.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    M("Male", "남성"),
    F("Female", "여성"),
    U("Unknown", "알 수 없음");

    private final String englishName;
    private final String koreanName;
}
