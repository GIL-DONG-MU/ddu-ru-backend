package com.dduru.gildongmu.auth.utils;

import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public final class OauthResponseUtils {

    private OauthResponseUtils() {}

    public static void validateResponse(Object response, String operation) {
        if (response == null) {
            log.error("{} 응답이 null입니다.", operation);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED,
                    String.format("%s 응답을 받지 못했습니다.", operation));
        }
    }

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
