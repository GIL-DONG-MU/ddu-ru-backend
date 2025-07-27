package com.dduru.gildongmu.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class KakaoAccount {
    private String email;

    @JsonProperty("profile_nickname_needs_agreement")
    private Boolean profileNicknameNeedsAgreement;

    @JsonProperty("profile_image_needs_agreement")
    private Boolean profileImageNeedsAgreement;

    private KakaoProfile profile;

    private String gender;

    @JsonProperty("gender_needs_agreement")
    private Boolean genderNeedsAgreement;

    @JsonProperty("age_range")
    private String ageRange;

    @JsonProperty("age_range_needs_agreement")
    private Boolean ageRangeNeedsAgreement;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("phone_number_needs_agreement")
    private Boolean phoneNumberNeedsAgreement;
}
