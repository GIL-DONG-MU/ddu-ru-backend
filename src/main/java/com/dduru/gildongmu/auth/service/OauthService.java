package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.user.domain.enums.OauthType;

public interface OauthService {
    OauthType getLoginType();
    OauthUserInfo verifyIdToken(String idToken);
}
