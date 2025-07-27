package com.dduru.gildongmu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String name;
    private String email;
    private String profileImage;
    private String gender;
    private String ageRange;
    private String phoneNumber;
}
