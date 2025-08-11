package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.enums.OauthType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoTest {

    @Test
    void 닉네임이_있는_경우_UserInfo는_닉네임을_반환한다() {
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
        UserInfo userInfo = UserInfo.from(user);

        // then
        assertThat(userInfo.name()).isEqualTo("닉네임");
        assertThat(userInfo.id()).isEqualTo(user.getId());
        assertThat(userInfo.profileImage()).isEqualTo(user.getProfileImage());
        assertThat(userInfo.gender()).isEqualTo(user.getGender().name());
        assertThat(userInfo.ageRange()).isEqualTo(user.getAgeRange().name());
    }

    @Test
    void 닉네임이_없는_경우_UserInfo는_이름을_반환한다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname(null)
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .gender(Gender.F)
                .ageRange(AgeRange.AGE_30s)
                .build();

        // when
        UserInfo userInfo = UserInfo.from(user);

        // then
        assertThat(userInfo.name()).isEqualTo("실제이름");
        assertThat(userInfo.id()).isEqualTo(user.getId());
        assertThat(userInfo.profileImage()).isEqualTo(user.getProfileImage());
        assertThat(userInfo.gender()).isEqualTo(user.getGender().name());
        assertThat(userInfo.ageRange()).isEqualTo(user.getAgeRange().name());
    }

    @Test
    void 닉네임이_빈_문자열인_경우_UserInfo는_이름을_반환한다() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("실제이름")
                .nickname("   ")
                .profileImage("http://example.com/profile.jpg")
                .oauthId("12345")
                .oauthType(OauthType.GOOGLE)
                .gender(Gender.M)
                .ageRange(AgeRange.AGE_20s)
                .build();

        // when
        UserInfo userInfo = UserInfo.from(user);

        // then
        assertThat(userInfo.name()).isEqualTo("실제이름");
    }
}