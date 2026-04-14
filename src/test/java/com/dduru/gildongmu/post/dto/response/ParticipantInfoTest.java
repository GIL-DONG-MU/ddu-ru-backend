package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.BgColor;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ParticipantInfo 테스트")
class ParticipantInfoTest {

    @Test
    @DisplayName("DEFAULT 타입이면 resolver가 계산한 기본 이미지 URL을 응답에 사용한다")
    void from_defaultType_usesResolvedDefaultImageUrl() {
        User user = createUser("default@example.com", "기본유저", "oauth-default", OauthType.KAKAO);
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
        ReflectionTestUtils.setField(bgColor, "id", 7L);
        profile.updateAvatar(avatarProfile);
        ReflectionTestUtils.setField(profile, "bgColor", bgColor);
        profile.updateProfile("기본닉", null, ProfileImageType.DEFAULT, null, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);

        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/default.png");

        ParticipantInfo participantInfo = ParticipantInfo.from(user, true, profileImageResolver);

        assertThat(participantInfo.profileImage().type()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(participantInfo.profileImage().url()).isEqualTo("https://example.com/default.png");
        assertThat(participantInfo.profileImage().bgColorId()).isNull();
        assertThat(participantInfo.isHost()).isTrue();
        verify(profileImageResolver).resolve(profile);
    }

    @Test
    @DisplayName("AVATAR 타입이면 resolver가 계산한 아바타 이미지 URL을 응답에 사용한다")
    void from_avatarType_usesResolvedAvatarImageUrl() {
        User user = createUser("avatar@example.com", "아바타유저", "oauth-avatar", OauthType.GOOGLE);
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
        ReflectionTestUtils.setField(bgColor, "id", 3L);
        profile.updateAvatar(avatarProfile);
        profile.updateProfile("아바타닉", null, ProfileImageType.AVATAR, bgColor, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);

        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/avatar.png");

        ParticipantInfo participantInfo = ParticipantInfo.from(user, false, profileImageResolver);

        assertThat(participantInfo.profileImage().type()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(participantInfo.profileImage().url()).isEqualTo("https://example.com/avatar.png");
        assertThat(participantInfo.profileImage().bgColorId()).isEqualTo(3L);
        assertThat(participantInfo.isHost()).isFalse();
        verify(profileImageResolver).resolve(profile);
    }

    private User createUser(String email, String name, String oauthId, OauthType oauthType) {
        return User.builder()
                .email(email)
                .name(name)
                .oauthId(oauthId)
                .oauthType(oauthType)
                .build();
    }
}
