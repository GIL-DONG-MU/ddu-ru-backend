package com.dduru.gildongmu.user.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 닉네임 형식 검증을 위한 복합 어노테이션입니다.
 * 검증 규칙:
 * - 필수값 (빈 값 불가)
 * - 2자 이상 12자 이하
 * - 한글, 영문, 숫자, 공백만 허용
 * - 연속 공백 불가
 * - 금칙어 불가
 * 처리하기 어려운 검증은 {@link NicknameConstraintValidator}에서 처리합니다.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameConstraintValidator.class)
@NotBlank(message = "닉네임은 필수입니다.")
@Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
@Pattern(regexp = "^[a-zA-Z0-9가-힣 ]*$", message = "닉네임은 한글, 영문, 숫자, 공백만 사용할 수 있습니다.")
public @interface ValidNickname {
    String message() default "닉네임 형식이 올바르지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
