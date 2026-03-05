package com.dduru.gildongmu.profile.domain;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Profile 닉네임 검증 테스트")
class ProfileTest {
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile(User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("oauth-id")
                .oauthType(OauthType.KAKAO)
                .build());
    }

    @Test
    @DisplayName("유효한 닉네임이면 변경 성공")
    void updateNickname_유효한닉네임_성공() {
        profile.updateNickname("용감한 여행자1234");

        assertThat(profile.getNickname()).isEqualTo("용감한 여행자1234");
    }

    @Test
    @DisplayName("닉네임이 null 또는 공백이면 NICKNAME_NOT_BLANK 예외")
    void updateNickname_nullOrBlank_예외() {
        assertThatThrownBy(() -> profile.updateNickname(null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_NOT_BLANK);

        assertThatThrownBy(() -> profile.updateNickname("   "))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_NOT_BLANK);
    }

    @Test
    @DisplayName("닉네임 길이가 2~14자가 아니면 NICKNAME_INVALID_LENGTH 예외")
    void updateNickname_길이부적합_예외() {
        assertThatThrownBy(() -> profile.updateNickname("a"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_INVALID_LENGTH);

        assertThatThrownBy(() -> profile.updateNickname("abcdefghijklmnop"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_INVALID_LENGTH);
    }

    @Test
    @DisplayName("허용되지 않는 문자가 포함되면 NICKNAME_INVALID_CHARACTERS 예외")
    void updateNickname_허용되지않는문자_예외() {
        assertThatThrownBy(() -> profile.updateNickname("길동무!"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_INVALID_CHARACTERS);
    }
}
