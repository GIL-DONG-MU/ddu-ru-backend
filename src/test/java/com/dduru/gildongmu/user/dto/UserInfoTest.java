package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UserInfo 테스트")
class UserInfoTest {

    @Test
    @DisplayName("DEFAULT 타입이면 기본 이미지 URL을 응답에 사용한다")
    void from_DEFAULT_usesDefaultImageUrl() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("테스터")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("테스트닉", null, ProfileImageType.DEFAULT, null, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/default.png");

        // when
        UserInfo userInfo = UserInfo.from(user, profileImageResolver);

        // then
        assertThat(userInfo.profileImageType()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(userInfo.profileImage()).isEqualTo("https://example.com/default.png");
    }

    @Test
    @DisplayName("UPLOADED 타입이면 저장된 업로드 URL을 응답에 사용한다")
    void from_UPLOADED_usesUploadedImageUrl() {
        // given
        User user = User.builder()
                .email("test2@example.com")
                .name("테스터2")
                .oauthId("oauth-2")
                .oauthType(OauthType.GOOGLE)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("업로드닉", "https://example.com/uploaded.png", ProfileImageType.UPLOADED, null, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/uploaded.png");

        // when
        UserInfo userInfo = UserInfo.from(user, profileImageResolver);

        // then
        assertThat(userInfo.profileImageType()).isEqualTo(ProfileImageType.UPLOADED);
        assertThat(userInfo.profileImage()).isEqualTo("https://example.com/uploaded.png");
    }
}
