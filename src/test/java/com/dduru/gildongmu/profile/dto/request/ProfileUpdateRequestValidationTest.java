package com.dduru.gildongmu.profile.dto.request;

import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProfileUpdateRequest 검증 테스트")
class ProfileUpdateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("AVATAR 타입에서 bgColorId가 없으면 @AssertTrue 검증이 실패한다")
    void validate_avatarWithoutBgColorId_failsByAssertTrue() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "아바타닉네임",
                null,
                ProfileImageType.AVATAR,
                null,
                "소개글"
        );

        // when
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .contains("bgColorIdValid");
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("AVATAR 타입에는 bgColorId가 필요합니다.");
    }

    @Test
    @DisplayName("UPLOADED 타입에서는 bgColorId가 없어도 @AssertTrue 검증을 통과한다")
    void validate_uploadedWithoutBgColorId_passesAssertTrue() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "업로드닉네임",
                "https://example.com/uploaded.png",
                ProfileImageType.UPLOADED,
                null,
                "소개글"
        );

        // when
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .doesNotContain("bgColorIdValid");
    }

    @Test
    @DisplayName("DEFAULT 타입에서는 bgColorId가 없어도 @AssertTrue 검증을 통과한다")
    void validate_defaultWithoutBgColorId_passesAssertTrue() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "기본닉네임",
                null,
                ProfileImageType.DEFAULT,
                null,
                "소개글"
        );

        // when
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .doesNotContain("bgColorIdValid");
    }
}
