package com.dduru.gildongmu.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통 (COMMON)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    JSON_CONVERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 변환 중 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

    // 인증 (AUTH)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 인증 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_SOCIAL_LOGIN(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 다른 소셜 계정으로 가입된 이메일입니다."),

    // 휴대폰 인증 (VERIFICATION)
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송에 실패했습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "재발송 제한 시간이 지나지 않았습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "인증 정보를 찾을 수 없습니다."),
    VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "검증 시도 횟수를 초과했습니다."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 완료된 인증입니다."),
    DAILY_SMS_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "일일 SMS 발송 한도를 초과했습니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다."),
    SMS_PROVIDER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 서비스에 일시적인 오류가 발생했습니다."),

    // 닉네임 (NICKNAME)
    NICKNAME_NOT_BLANK(HttpStatus.BAD_REQUEST, "닉네임은 공백일 수 없습니다."),
    NICKNAME_INVALID_LENGTH(HttpStatus.BAD_REQUEST, "닉네임은 2자 이상 14자 이하로 입력해주세요."),
    NICKNAME_INVALID_CHARACTERS(HttpStatus.BAD_REQUEST, "닉네임은 한글, 영어, 숫자만 사용 가능합니다."),
    NICKNAME_CONSECUTIVE_SPACES(HttpStatus.BAD_REQUEST, "공백은 단어 사이에 한 번만 사용할 수 있습니다."),
    NICKNAME_CONTAINS_BAD_WORD(HttpStatus.BAD_REQUEST, "닉네임에 부적절한 단어가 포함되어 있습니다."),
    NICKNAME_ALREADY_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    NICKNAME_CONTAINS_EMOJI_OR_SYMBOL(HttpStatus.BAD_REQUEST, "닉네임에 이모지 또는 특수 기호는 사용할 수 없습니다."),

    // 설문 (SURVEY)
    SURVEY_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "설문 결과를 찾을 수 없습니다."),
    AVATAR_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "아바타 프로필을 찾을 수 없습니다."),
    SURVEY_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "설문조사가 이미 완료된 상태에서는 스킵할 수 없습니다."),

    // 프로필 (PROFILE)
    BG_COLOR_NOT_FOUND(HttpStatus.NOT_FOUND, "배경색을 찾을 수 없습니다."),
    INVALID_PROFILE_IMAGE_URL(HttpStatus.BAD_REQUEST, "프로필 이미지 URL이 유효하지 않습니다."),
    USER_ONBOARDING_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 온보딩 정보를 찾을 수 없습니다."),

    // 사용자 (USER)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저의 프로필을 찾을 수 없습니다."),

    // 게시글 (POST)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시글에 대한 접근 권한이 없습니다."),
    INVALID_POST_DATE(HttpStatus.BAD_REQUEST, "잘못된 날짜 설정입니다."),
    INVALID_RECRUIT_CAPACITY(HttpStatus.BAD_REQUEST, "잘못된 모집 인원 설정입니다."),
    TRAVEL_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 시작된 여행입니다."),
    TRAVEL_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "이미 종료된 여행입니다."),
    RECRUIT_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "모집 마감된 게시글은 수정할 수 없습니다."),
    INVALID_AGE_RANGE(HttpStatus.BAD_REQUEST, "최대 연령은 최소 연령보다 크거나 같아야 합니다."),
    INVALID_BUDGET_RANGE(HttpStatus.BAD_REQUEST, "최대 예산은 최소 예산보다 커야 합니다."),
    RECRUIT_COUNT_EXCEED_CAPACITY(HttpStatus.BAD_REQUEST, "모집 정원을 초과할 수 없습니다."),
    RECRUIT_COUNT_BELOW_ZERO(HttpStatus.BAD_REQUEST, "모집 인원이 0 이하가 될 수 없습니다."),
    IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 3장까지 업로드할 수 있습니다."),
    INVALID_POST_STATUS(HttpStatus.BAD_REQUEST, "모집이 완료된 게시글은 모집 상태를 변경할 수 없습니다."),

    // 댓글 (COMMENT)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "부모 댓글이 현재 게시글에 속해있지 않습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "댓글에 대한 접근 권한이 없습니다."),

    // 여행지 (DESTINATION)
    DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."),

    // 참여신청 (PARTICIPATION)
    PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참여신청을 찾을 수 없습니다."),
    DUPLICATE_PARTICIPATION(HttpStatus.BAD_REQUEST, "이미 참여신청한 게시글입니다."),
    RECRUITMENT_CLOSED(HttpStatus.BAD_REQUEST, "모집이 마감되었거나 정원이 찼습니다."),
    SELF_PARTICIPATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 게시글에는 참여신청할 수 없습니다."),
    PARTICIPATION_POST_MISMATCH(HttpStatus.BAD_REQUEST, "해당 참여신청은 해당 게시글에 속해있지 않습니다."),

    // 신고 (REPORT)
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."),
    SELF_POST_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 게시글은 신고할 수 없습니다."),
    DUPLICATE_POST_REPORT(HttpStatus.CONFLICT, "이미 신고한 게시글입니다."),

    // 파일 (FILE)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 확장자입니다."),

    // 채팅 (CHAT)
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방 멤버를 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),
    CHAT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방 접근 권한이 없습니다."),
    CHAT_ROOM_CLOSED(HttpStatus.BAD_REQUEST, "종료된 채팅방입니다."),
    CHAT_ROOM_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "채팅방 정원을 초과했습니다."),
    CHAT_NOT_GROUP_ROOM(HttpStatus.BAD_REQUEST, "그룹 채팅방이 아닙니다."),
    CHAT_INVITE_HOST_ONLY(HttpStatus.FORBIDDEN, "방장만 멤버를 초대할 수 있습니다."),
    NOT_SELF_CHAT(HttpStatus.FORBIDDEN, "자신과의 채팅은 허용되지 않습니다."),
    CHAT_ROOM_INVITE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게시글 작성자만 그룹 채팅 멤버를 추가할 수 있습니다.");

    private final int status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }
}
