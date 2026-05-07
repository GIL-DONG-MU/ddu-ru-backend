package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.config.S3Properties;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyService 테스트")
class JourneyServiceTest {
    private static final String S3_HOST = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com";

    @Mock
    private JourneyRepository journeyRepository;

    private JourneyService journeyService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("dummy-bucket");
        s3Properties.setRegion("ap-northeast-2");
        journeyService = new JourneyService(journeyRepository, new S3ImageUrlValidator(s3Properties));
    }

    @Nested
    @DisplayName("나의 여정 기본 정보 수정")
    class UpdateBasicInfo {

        @Test
        @DisplayName("호스트는 제목과 대표 사진을 수정할 수 있다")
        void hostCanUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    "제주 우리 여행",
                    S3_HOST + "/journeys/journey-updated.png"
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.title()).isEqualTo("제주 우리 여행");
            assertThat(response.photoUrl()).isEqualTo(S3_HOST + "/journeys/journey-updated.png");
            assertThat(journey.getTitle()).isEqualTo("제주 우리 여행");
            assertThat(journey.getPhotoUrl()).isEqualTo(S3_HOST + "/journeys/journey-updated.png");
        }

        @Test
        @DisplayName("active 멤버도 제목과 대표 사진을 수정할 수 있다")
        void activeMemberCanUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 20L;
            Journey journey = createJourney(journeyId, 10L);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    "멤버가 바꾼 제목",
                    S3_HOST + "/journeys/member-updated.png"
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.title()).isEqualTo("멤버가 바꾼 제목");
            assertThat(response.photoUrl()).isEqualTo(S3_HOST + "/journeys/member-updated.png");
            assertThat(journey.getTitle()).isEqualTo("멤버가 바꾼 제목");
            assertThat(journey.getPhotoUrl()).isEqualTo(S3_HOST + "/journeys/member-updated.png");
        }

        @Test
        @DisplayName("비활성 멤버는 수정할 수 없다")
        void inactiveMemberCannotUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 20L;
            Journey journey = createJourney(journeyId, 10L);
            JourneyMember member = JourneyMember.createMember(journey, createUser(userId, "member"));
            member.remove();
            JourneyUpdateRequest request = new JourneyUpdateRequest("새 제목", null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);
        }

        @Test
        @DisplayName("수정 값이 모두 비어 있으면 예외가 발생한다")
        void throwsWhenNoPatchValueProvided() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest("   ", null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_EMPTY_PATCH);
        }

        @Test
        @DisplayName("제목 길이가 범위를 벗어나면 예외가 발생한다")
        void throwsWhenTitleLengthIsInvalid() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest("😀😀😀😀", null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INVALID_TITLE_LENGTH);
        }

        @Test
        @DisplayName("대표 사진 URL 형식이 잘못되면 예외가 발생한다")
        void throwsWhenPhotoUrlIsInvalid() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, "http:test");

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INVALID_PHOTO_URL);
        }

        @Test
        @DisplayName("대표 사진만 수정할 수 있다")
        void canUpdateOnlyPhoto() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    null,
                    S3_HOST + "/journeys/journey-photo-only.png"
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(Optional.of(journey));

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.title()).isEqualTo("제주도 2박 3일 여행");
            assertThat(response.photoUrl()).isEqualTo(S3_HOST + "/journeys/journey-photo-only.png");
        }
    }

    private Journey createJourney(Long journeyId, Long ownerId) {
        Post post = createPost(100L, ownerId, "제주도 2박 3일 여행", "제주");
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        return journey;
    }

    private Post createPost(Long postId, Long ownerId, String title, String destinationCity) {
        User owner = createUser(ownerId, "owner-" + ownerId);
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city(destinationCity)
                .build();

        Post post = Post.createPost(
                owner,
                destination,
                title,
                "나의 여정 기본 정보 수정 테스트용 본문입니다.",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                4,
                LocalDate.now().plusDays(6),
                Gender.U,
                true,
                null,
                null,
                S3_HOST + "/posts/photo.png",
                "[]",
                CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private User createUser(Long userId, String name) {
        User user = User.builder()
                .email(name + "@example.com")
                .name(name)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
