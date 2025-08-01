package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOauthService implements OauthService {

    protected final WebClient webClient;

    protected void handleOauthException(Exception e, String operation) {
        log.error("{} 중 오류 발생", operation, e);
        throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED,
                String.format("%s에 실패했습니다. 다시 시도해주세요.", operation));
    }

    protected void validateResponse(Object response, String operation) {
        if (response == null) {
            log.error("{} 응답이 null입니다.", operation);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED,
                    String.format("%s 응답을 받지 못했습니다.", operation));
        }
    }

    protected String buildUrlParams(String... keyValues) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) params.append("&");
            params.append(keyValues[i]).append("=").append(keyValues[i + 1]);
        }
        return params.toString();
    }

    protected abstract String getClientId();
    protected abstract String getClientSecret();
    protected abstract String getRedirectUri();
    protected abstract String getAuthUrl();
    protected abstract String getTokenUrl();
    protected abstract String getUserInfoUrl();
    protected abstract String getScope();
}
