package com.dduru.gildongmu.common.dto;

public record ApiResponse<T>(
        Integer status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> success(Integer status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
}
