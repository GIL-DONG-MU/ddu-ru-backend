package com.dduru.gildongmu.auth.domain;

import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.enums.OauthType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void 닉네임이_있을_때_getDisplayName은_닉네임을_반환한다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname("닉네임")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        // when
        String displayName = user.getDisplayName();

        // then
        assertThat(displayName).isEqualTo("닉네임");
    }

    @Test
    void 닉네임이_null일_때_getDisplayName은_이름을_반환한다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname(null)
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        // when
        String displayName = user.getDisplayName();

        // then
        assertThat(displayName).isEqualTo("실제이름");
    }

    @Test
    void 닉네임이_빈_문자열일_때_getDisplayName은_이름을_반환한다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname("   ")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        // when
        String displayName = user.getDisplayName();

        // then
        assertThat(displayName).isEqualTo("실제이름");
    }

    @Test
    void updateNickname_메서드로_닉네임을_변경할_수_있다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname("기존닉네임")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        // when
        user.updateNickname("새로운닉네임");

        // then
        assertThat(user.getNickname()).isEqualTo("새로운닉네임");
        assertThat(user.getDisplayName()).isEqualTo("새로운닉네임");
    }
}