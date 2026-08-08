package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;

public record HomeHostResponse(
        String nickname,
        ProfileImageInfo profileImageInfo,
        int age,
        Gender gender
) {
}
