package com.dduru.gildongmu.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증 관련
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다."),
    UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "AUTH_003", "지원하지 않는 소셜 로그인입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_004", "소셜 로그인에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_005", "인증되지 않은 사용자입니다"),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),

    // 게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다"),
    DUPLICATE_POST(HttpStatus.CONFLICT, "POST_002", "해당 여행지에 대한 게시글이 이미 존재합니다"),
    INVALID_POST_DATE(HttpStatus.BAD_REQUEST, "POST_003", "잘못된 날짜 설정입니다"),

    // 여행지 관련
    DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DEST_001", "여행지를 찾을 수 없습니다"),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
