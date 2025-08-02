package com.dduru.gildongmu.auth.utils;

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
        log.error("{} 중 오류 발생", operation, e);
        throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED,
                String.format("%s에 실패했습니다. 다시 시도해주세요.", operation));
    }

    @SuppressWarnings("unchecked")
    public static Optional<String> extractFieldFromResponse(Map<String, Object> response,
                                                            String fieldName,
                                                            String valueKey) {
        try {
            List<Map<String, Object>> fieldList = (List<Map<String, Object>>) response.get(fieldName);
            if (fieldList != null && !fieldList.isEmpty()) {
                Map<String, Object> field = fieldList.get(0);
                return Optional.ofNullable((String) field.get(valueKey));
            }
        } catch (Exception e) {
            log.warn("{} 정보 추출 실패: {}", fieldName, e.getMessage());
        }
        return Optional.empty();
    }
}
