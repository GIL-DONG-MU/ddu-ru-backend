package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.exception.UnsupportedOauthTypeException;
import com.dduru.gildongmu.user.enums.OauthType;
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
            throw UnsupportedOauthTypeException.of(loginType.name());
        }
        return service;
    }

}
