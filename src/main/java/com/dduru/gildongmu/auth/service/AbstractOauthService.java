package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.utils.OauthResponseUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOauthService implements OauthService {

    protected void handleOauthException(Exception e, String operation) {
        OauthResponseUtils.handleException(e, operation);
    }

    protected abstract String getClientId();
}
