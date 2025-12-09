package com.dduru.gildongmu.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(
        int status,
        T data
) {
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(HttpStatus.OK.value(), data);
    }

    public static <T> ApiResult<T> created(T data) {
        return new ApiResult<>(HttpStatus.CREATED.value(), data);
    }

    public static ApiResult<Void> noContent() {
        return new ApiResult<>(HttpStatus.NO_CONTENT.value(), null);
    }

    /* 커스텀 HTTP 상태 코드와 데이터 */
    public static <T> ApiResult<T> of(HttpStatus status, T data) {
        return new ApiResult<>(status.value(), data);
    }

    public static <T> ApiResult<T> of(int statusCode, T data) {
        return new ApiResult<>(statusCode, data);
    }
}
