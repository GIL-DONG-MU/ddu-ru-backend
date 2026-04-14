package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
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
        assertThat(userInfo.profileImage().type()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(userInfo.profileImage().url()).isEqualTo("https://example.com/default.png");
        assertThat(userInfo.profileImage().bgColorId()).isNull();
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
        assertThat(userInfo.profileImage().type()).isEqualTo(ProfileImageType.UPLOADED);
        assertThat(userInfo.profileImage().url()).isEqualTo("https://example.com/uploaded.png");
        assertThat(userInfo.profileImage().bgColorId()).isNull();
    }

    @Test
    @DisplayName("AVATAR 타입이면 profileImage 객체에 bgColor 정보를 함께 담는다")
    void from_AVATAR_includesAvatarAndBgColor() {
        User user = User.builder()
                .email("test3@example.com")
                .name("테스터3")
                .oauthId("oauth-3")
                .oauthType(OauthType.GOOGLE)
                .build();
        Profile profile = new Profile(user);

        AvatarProfile avatarProfile = AvatarProfile.builder()
                .avatarType(AvatarType.TTUR_DASOM)
                .displayName("다솜")
                .speechBubbleText("설명")
                .descriptionLine1("성격")
                .descriptionLine2("강점")
                .descriptionLine3("팁")
                .imageUrl("https://example.com/avatar.png")
                .tags("[]")
                .build();
        BgColor bgColor = BgColor.builder()
                .hexCode("#F5E6C8")
                .displayOrder(1)
                .build();
        ReflectionTestUtils.setField(bgColor, "id", 9L);

        profile.updateAvatar(avatarProfile);
        profile.updateProfile("아바타닉", null, ProfileImageType.AVATAR, bgColor, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);

        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/avatar.png");

        UserInfo userInfo = UserInfo.from(user, profileImageResolver);

        assertThat(userInfo.profileImage().type()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(userInfo.profileImage().url()).isEqualTo("https://example.com/avatar.png");
        assertThat(userInfo.profileImage().bgColorId()).isEqualTo(9L);
    }
}
