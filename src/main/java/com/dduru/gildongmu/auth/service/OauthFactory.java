package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.enums.OauthType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OauthFactory {

    private final Map<OauthType, OauthService> oauthServices;

    public OauthFactory(List<OauthService> oauthServices) {
        this.oauthServices = oauthServices.stream()
                .collect(Collectors.toMap(
                        OauthService::getLoginType,
                        service -> service
                ));
    }

    public OauthService getOauthService(OauthType loginType) {
        OauthService service = oauthServices.get(loginType);
        if (service == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다: " + loginType);
        }
        return service;
    }

    public OauthService getOauthService(String loginType) {
        return getOauthService(OauthType.fromValue(loginType));
    }
}
