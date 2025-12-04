package com.dduru.gildongmu.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    M("Male", "남성"),
    F("Female", "여성"),
    U("Unknown", "상관없음");

    private final String englishName;
    private final String koreanName;

    public static Gender from(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return null;
        }

        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
