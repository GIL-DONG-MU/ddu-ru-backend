package com.dduru.gildongmu.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoAccount(
        String email,

        @JsonProperty("profile_nickname_needs_agreement")
        Boolean profileNicknameNeedsAgreement,

        @JsonProperty("profile_image_needs_agreement")
        Boolean profileImageNeedsAgreement,

        KakaoProfile profile,

        String gender,

        @JsonProperty("gender_needs_agreement")
        Boolean genderNeedsAgreement,

        @JsonProperty("age_range")
        String ageRange,

        @JsonProperty("age_range_needs_agreement")
        Boolean ageRangeNeedsAgreement,

        @JsonProperty("phone_number")
        String phoneNumber,

        @JsonProperty("phone_number_needs_agreement")
        Boolean phoneNumberNeedsAgreement
) {
}
