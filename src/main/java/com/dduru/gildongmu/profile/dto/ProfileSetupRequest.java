package com.dduru.gildongmu.profile.dto;

import com.dduru.gildongmu.profile.validator.ValidNickname;

public record ProfileSetupRequest(
        @ValidNickname
        String nickname,
        String gender,
        String phoneNumber,
        String birthday,
        String verificationToken
) {
}
