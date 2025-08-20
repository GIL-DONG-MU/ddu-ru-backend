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

    // 게시글 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다"),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_002", "게시글에 대한 접근 권한이 없습니다"),
    INVALID_POST_DATE(HttpStatus.BAD_REQUEST, "POST_003", "잘못된 날짜 설정입니다"),
    INVALID_RECRUIT_CAPACITY(HttpStatus.BAD_REQUEST, "POST_004", "잘못된 모집 인원 설정입니다."),
    TRAVEL_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "POST_005", "이미 시작된 여행입니다."),
    TRAVEL_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "POST_006", "이미 종료된 여행입니다."),
    INVALID_AGE_RANGE(HttpStatus.BAD_REQUEST, "POST_007", "최대 연령은 최소 연령보다 크거나 같아야 합니다."),
    INVALID_BUDGET_RANGE(HttpStatus.BAD_REQUEST, "POST_008", "최대 예산은 최소 예산보다 커야 합니다."),
    RECRUIT_COUNT_EXCEED_CAPACITY(HttpStatus.BAD_REQUEST, "POST_009", "모집 정원을 초과할 수 없습니다."),
    RECRUIT_COUNT_BELOW_ZERO(HttpStatus.BAD_REQUEST, "POST_010", "모집 인원이 0 이하가 될 수 없습니다."),

    // 여행지 관련
    DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DEST_001", "여행지를 찾을 수 없습니다"),

    // 참여신청 관련
    PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PAR_001", "참여신청을 찾을 수 없습니다"),
    DUPLICATE_PARTICIPATION(HttpStatus.BAD_REQUEST, "PAR_002", "이미 참여신청한 게시글입니다"),
    RECRUITMENT_CLOSED(HttpStatus.BAD_REQUEST, "PAR_003", "모집이 마감되었거나 정원이 찼습니다"),
    SELF_PARTICIPATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAR_004", "자신의 게시글에는 참여신청할 수 없습니다"),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다."),
    JSON_CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "JSON 변환 중 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
