package com.dduru.gildongmu.common.dto;

public record ApiResult<T>(
        Integer status,
        String message,
        T data
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "OK", data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    public static <T> ApiResult<T> success(Integer status, String message, T data) {
        return new ApiResult<>(status, message, data);
    }
}
