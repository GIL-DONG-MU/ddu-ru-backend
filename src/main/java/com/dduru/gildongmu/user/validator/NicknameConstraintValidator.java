package com.dduru.gildongmu.user.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * {@link ValidNickname} 어노테이션의 실제 검증 로직을 담당합니다.
 * ex. 금칙어 검증 등
 * 기본 어노테이션(@NotBlank, @Size, @Pattern)으로 처리하기 어려운 검증을 수행합니다.
 */
public class NicknameConstraintValidator implements ConstraintValidator<ValidNickname, String> {

    private static final List<String> BAD_WORDS = List.of("쓰레기"); // TODO: 교체 예정

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        for (String badWord : BAD_WORDS) {
            if (nickname.contains(badWord)) {
                setCustomMessage(context, "닉네임에 부적절한 단어가 포함되어 있습니다.");
                return false;
            }
        }

        return true;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
