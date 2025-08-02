package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.enums.OauthType;

public interface OauthService {
    OauthUserInfo getUserInfo(String accessToken);
    String getAuthorizationUrl();
    String getAccessToken(String code);
    OauthType getLoginType();
    OauthUserInfo verifyIdToken(String idToken);
}
