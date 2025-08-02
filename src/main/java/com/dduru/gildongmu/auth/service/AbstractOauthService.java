package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.utils.OauthConstants;
import com.dduru.gildongmu.auth.utils.OauthResponseUtils;
import com.dduru.gildongmu.auth.utils.UrlParamBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOauthService implements OauthService {

    protected final WebClient webClient;

    protected void handleOauthException(Exception e, String operation) {
        OauthResponseUtils.handleException(e, operation);
    }

    protected void validateResponse(Object response, String operation) {
        OauthResponseUtils.validateResponse(response, operation);
    }

    protected String buildUrlParams(String... keyValues) {
        UrlParamBuilder builder = UrlParamBuilder.create();
        for (int i = 0; i < keyValues.length; i += 2) {
            builder.add(keyValues[i], keyValues[i + 1]);
        }
        return builder.build();
    }

    protected String buildTokenRequestBody(String code) {
        return buildUrlParams(
                "grant_type", OauthConstants.Common.GRANT_TYPE_AUTH_CODE,
                "client_id", getClientId(),
                "client_secret", getClientSecret(),
                "code", code,
                "redirect_uri", getRedirectUri()
        );
    }

    protected abstract String getClientId();
    protected abstract String getClientSecret();
    protected abstract String getRedirectUri();
    protected abstract String getAuthUrl();
    protected abstract String getTokenUrl();
    protected abstract String getUserInfoUrl();
    protected abstract String getScope();
}
