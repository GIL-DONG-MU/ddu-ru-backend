package com.dduru.gildongmu.auth.dto;

import com.dduru.gildongmu.auth.enums.OauthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthUserInfo {
    private String oauthId;
    private String email;
    private String name;
    private String profileImage;
    private OauthType loginType;
    private String gender;
    private String ageRange;
    private String phoneNumber;
}
