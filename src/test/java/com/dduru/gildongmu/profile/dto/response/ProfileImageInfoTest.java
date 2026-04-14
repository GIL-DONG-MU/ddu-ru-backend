package com.dduru.gildongmu.profile.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProfileImageInfo 테스트")
class ProfileImageInfoTest {

    @Test
    @DisplayName("AVATAR 타입이 아니면 bgColorId를 노출하지 않는다")
    void from_nonAvatarType_hidesBgColorId() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default.png");

        ProfileImageInfo profileImageInfo = ProfileImageInfo.from(
                ProfileImageType.DEFAULT,
                null,
                "https://example.com/avatar.png",
                3L,
                resolver
        );

        assertThat(profileImageInfo.type()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(profileImageInfo.url()).isEqualTo("https://example.com/default.png");
        assertThat(profileImageInfo.bgColorId()).isNull();
    }

    @Test
    @DisplayName("Profile 엔티티에서 프로필 이미지 정보를 조립한다")
    void from_profile_buildsProfileImageInfo() {
        ProfileImageResolver resolver = new ProfileImageResolver();
        ReflectionTestUtils.setField(resolver, "defaultProfileImageUrl", "https://example.com/default.png");

        User user = User.builder()
                .email("test@example.com")
                .name("테스터")
                .oauthId("oauth-1")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        profile.updateProfile("테스트닉", "https://example.com/uploaded.png", ProfileImageType.UPLOADED, null, "소개");

        ProfileImageInfo profileImageInfo = ProfileImageInfo.from(profile, resolver);

        assertThat(profileImageInfo.type()).isEqualTo(ProfileImageType.UPLOADED);
        assertThat(profileImageInfo.url()).isEqualTo("https://example.com/uploaded.png");
        assertThat(profileImageInfo.bgColorId()).isNull();
    }
}
