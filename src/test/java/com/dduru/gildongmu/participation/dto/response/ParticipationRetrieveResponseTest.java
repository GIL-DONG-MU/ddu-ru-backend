package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.query.ParticipationRetrieveQueryResult;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ParticipationRetrieveResponse 테스트")
class ParticipationRetrieveResponseTest {

    @Test
    @DisplayName("AVATAR 타입이면 bgColorId를 함께 응답한다")
    void from_avatarType_includesBgColor() {
        ParticipationRetrieveQueryResult queryResult = new ParticipationRetrieveQueryResult(
                1L,
                2L,
                "신청자",
                ProfileImageType.AVATAR,
                null,
                "https://example.com/avatar.png",
                5L,
                "안녕하세요",
                ParticipationStatus.PENDING,
                LocalDateTime.of(2026, 4, 14, 10, 0),
                null,
                null,
                null,
                10L,
                "제주도 같이 가실 분"
        );
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(ProfileImageType.AVATAR, null, "https://example.com/avatar.png"))
                .thenReturn("https://example.com/avatar.png");

        ParticipationRetrieveResponse response = ParticipationRetrieveResponse.from(queryResult, profileImageResolver);

        assertThat(response.profileImage().type()).isEqualTo(ProfileImageType.AVATAR);
        assertThat(response.profileImage().url()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.profileImage().bgColorId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("AVATAR 타입이 아니면 남아 있는 bgColor 값이 있어도 응답에는 노출하지 않는다")
    void from_nonAvatarType_hidesBgColor() {
        ParticipationRetrieveQueryResult queryResult = new ParticipationRetrieveQueryResult(
                1L,
                2L,
                "신청자",
                ProfileImageType.DEFAULT,
                null,
                "https://example.com/avatar.png",
                5L,
                "안녕하세요",
                ParticipationStatus.PENDING,
                LocalDateTime.of(2026, 4, 14, 10, 0),
                null,
                null,
                null,
                10L,
                "제주도 같이 가실 분"
        );
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        when(profileImageResolver.resolve(ProfileImageType.DEFAULT, null, "https://example.com/avatar.png"))
                .thenReturn("https://example.com/default.png");

        ParticipationRetrieveResponse response = ParticipationRetrieveResponse.from(queryResult, profileImageResolver);

        assertThat(response.profileImage().type()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(response.profileImage().url()).isEqualTo("https://example.com/default.png");
        assertThat(response.profileImage().bgColorId()).isNull();
    }
}
