package com.dduru.gildongmu.auth.dto.google;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserResponse {
    private String id;
    private String email;
    private String name;
    private String picture;
    private String locale;
    private boolean verified_email;
}
