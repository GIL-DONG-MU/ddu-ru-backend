package com.dduru.gildongmu.auth.utils;

import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OauthResponseUtils {

    private OauthResponseUtils() {}

    public static void handleException(Exception e, String operation) {
        if (e instanceof InvalidTokenException invalidTokenException) {
            log.warn("{} 중 토큰 검증 실패: {}", operation, invalidTokenException.getMessage());
            throw invalidTokenException;
        }

        log.error("{} 중 오류 발생", operation, e);
        throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED,
                String.format("%s에 실패했습니다. 다시 시도해주세요.", operation));
    }
}
