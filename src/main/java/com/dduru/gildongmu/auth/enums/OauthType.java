package com.dduru.gildongmu.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthType {
    KAKAO("kakao"),
    GOOGLE("google");

    private final String value;

    public static OauthType fromValue(String value) {
        for (OauthType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown oauth type: " + value);
    }
}
