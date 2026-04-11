package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
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
}
