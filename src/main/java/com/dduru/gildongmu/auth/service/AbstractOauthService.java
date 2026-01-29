package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.utils.OauthResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOauthService implements OauthService {

    protected final WebClient webClient;

    protected AbstractOauthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    protected void handleOauthException(Exception e, String operation) {
        OauthResponseUtils.handleException(e, operation);
    }

    protected abstract String getClientId();
}
