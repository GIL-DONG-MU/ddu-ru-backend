package com.dduru.gildongmu.auth.dto;

import com.dduru.gildongmu.auth.enums.OauthType;
import lombok.Builder;

@Builder
public record OauthUserInfo(
        String oauthId,
        String email,
        String name,
        String profileImage,
        OauthType loginType,
        String gender,
        String ageRange,
        String phoneNumber
) {
}
