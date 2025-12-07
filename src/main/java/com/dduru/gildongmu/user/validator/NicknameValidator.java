package com.dduru.gildongmu.user.validator;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NicknameValidator {

    private final UserRepository userRepository;

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 12;
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[a-zA-Z0-9가-힣]*$");
    private static final List<String> BAD_WORDS = List.of("바보", "멍청이", "쓰레기"); // 예시입니다.

    public void validate(String nickname) {
        validateNotBlank(nickname);
        validateLength(nickname);
        validateCharacters(nickname);
        validateNoBadWords(nickname);
        validateUniqueness(nickname);
    }

    private void validateNotBlank(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_NOT_BLANK);
        }
    }

    private void validateLength(String nickname) {
        if (nickname.length() < MIN_LENGTH || nickname.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_LENGTH);
        }
    }

    private void validateCharacters(String nickname) {
        if (!ALLOWED_CHARACTERS.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.NICKNAME_INVALID_CHARACTERS);
        }
    }

    private void validateNoBadWords(String nickname) {
        // TODO: 실제 서비스에 맞는 부적절한 단어 리스트로 교체 필요
        if (BAD_WORDS.stream().anyMatch(nickname::contains)) {
            throw new BusinessException(ErrorCode.NICKNAME_CONTAINS_BAD_WORD);
        }
    }

    private void validateUniqueness(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }
}
