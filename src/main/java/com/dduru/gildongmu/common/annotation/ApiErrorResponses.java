package com.dduru.gildongmu.common.annotation;

import com.dduru.gildongmu.common.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 엔드포인트에 자동으로 에러 응답을 추가하기 위한 어노테이션
 * ErrorCode enum을 지정하면 해당 ErrorCode의 HTTP 상태 코드가 자동으로 사용됩니다.
 * 
 * 사용 예시:
 * <pre>
 * {@code @ApiErrorResponses({ErrorCode.NICKNAME_ALREADY_TAKEN, ErrorCode.PROFILE_NOT_FOUND})}
 * ResponseEntity<ApiResult<Response>> method();
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorResponses {
    /**
     * 추가할 ErrorCode enum 목록 (예: ErrorCode.NICKNAME_ALREADY_TAKEN, ErrorCode.PROFILE_NOT_FOUND)
     * 각 ErrorCode의 HTTP 상태 코드가 자동으로 사용됩니다.
     */
    ErrorCode[] value();
}
