package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("프로필 이미지 resolver 테스트")
class ProfileImageResolverTest {

    @Test
    @DisplayName("AVATAR 타입이면 아바타 이미지 URL을 반환한다")
    void resolve_avatarType_returnsAvatarImageUrl() {
        // given
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default-profile.png");

        User user = User.builder()
                .email("test@example.com")
                .name("테스터")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("테스트닉", null, ProfileImageType.AVATAR, null, "소개");

        AvatarProfile avatarProfile = AvatarProfile.builder()
                .avatarType(AvatarType.TTUR_DASOM)
                .displayName("뚜르 스윗")
                .speechBubbleText("설명")
                .descriptionLine1("첫 문단")
                .descriptionLine2("둘째 문단")
                .descriptionLine3("셋째 문단")
                .imageUrl("https://example.com/avatar-sweet.png")
                .tags("[]")
                .build();
        profile.updateAvatar(avatarProfile);

        // when
        String resolved = resolver.resolve(profile);

        // then
        assertThat(resolved).isEqualTo("https://example.com/avatar-sweet.png");
    }

    @Test
    @DisplayName("AVATAR 타입인데 매핑된 아바타가 없으면 기본 이미지를 반환한다")
    void resolve_avatarTypeWithoutAvatar_returnsDefaultImageUrl() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default-profile.png");

        User user = User.builder()
                .email("test@example.com")
                .name("테스터")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("테스트닉", null, ProfileImageType.AVATAR, null, "소개");

        String resolved = resolver.resolve(profile);

        assertThat(resolved).isEqualTo("https://example.com/default-profile.png");
    }

    @Test
    @DisplayName("UPLOADED 타입인데 업로드 URL이 없으면 기본 이미지를 반환한다")
    void resolve_uploadedTypeWithoutUploadedUrl_returnsDefaultImageUrl() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default-profile.png");

        User user = User.builder()
                .email("test@example.com")
                .name("테스터")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("테스트닉", "https://example.com/uploaded.png", ProfileImageType.UPLOADED, null, "소개");
        ReflectionTestUtils.setField(profile, "uploadedImageUrl", null);

        String resolved = resolver.resolve(profile);

        assertThat(resolved).isEqualTo("https://example.com/default-profile.png");
    }

    @Test
    @DisplayName("쿼리 기반 resolve에서도 AVATAR 이미지가 없으면 기본 이미지를 반환한다")
    void resolve_queryAvatarWithoutImageUrl_returnsDefaultImageUrl() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default-profile.png");

        String resolved = resolver.resolve(ProfileImageType.AVATAR, null, null);

        assertThat(resolved).isEqualTo("https://example.com/default-profile.png");
    }

    @Test
    @DisplayName("쿼리 기반 resolve에서도 UPLOADED URL이 없으면 기본 이미지를 반환한다")
    void resolve_queryUploadedWithoutImageUrl_returnsDefaultImageUrl() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default-profile.png");

        String resolved = resolver.resolve(ProfileImageType.UPLOADED, null, null);

        assertThat(resolved).isEqualTo("https://example.com/default-profile.png");
    }
}
