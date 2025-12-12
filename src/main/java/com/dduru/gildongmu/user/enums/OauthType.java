package com.dduru.gildongmu.user.enums;

import com.dduru.gildongmu.auth.exception.UnsupportedOauthTypeException;
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
        throw UnsupportedOauthTypeException.of(value);
    }
}
