package com.dduru.gildongmu.user.validator;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

import java.util.List;
import java.util.regex.Pattern;

public final class NicknameValidator {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 12;
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[a-zA-Z0-9가-힣]*$");
    private static final Pattern EMOJI_OR_SYMBOL = Pattern.compile("[\\p{So}\\p{Cn}\\p{Cs}]");
    private static final List<String> RESERVED = List.of("root", "관리자", "서포트");
    private static final List<String> BAD_WORDS = List.of("바보", "멍청이", "쓰레기"); // 예시입니다.

    public static void validate(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_NOT_BLANK);
        }

        if (nickname.length() < MIN_LENGTH || nickname.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_LENGTH);
        }

        if (!ALLOWED_CHARACTERS.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_CHARACTERS);
        }

        if (EMOJI_OR_SYMBOL.matcher(nickname).find()) {
            throw new BusinessException(ErrorCode.NICKNAME_CONTAINS_EMOJI_OR_SYMBOL);
        }

        if (RESERVED.stream().anyMatch(nickname::equalsIgnoreCase)) {
            throw new BusinessException(ErrorCode.NICKNAME_IS_RESERVED);
        }

        if (BAD_WORDS.stream().anyMatch(nickname::contains)) {
            throw new BusinessException(ErrorCode.NICKNAME_CONTAINS_BAD_WORD);
        }
    }
}
