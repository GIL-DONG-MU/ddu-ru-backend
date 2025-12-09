package com.dduru.gildongmu.common.dto;

public record ApiResult<T>(
        Integer status,
        T data
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, data);
    }

    public static <T> ApiResult<T> success(Integer status, T data) {
        return new ApiResult<>(status, data);
    }
}
