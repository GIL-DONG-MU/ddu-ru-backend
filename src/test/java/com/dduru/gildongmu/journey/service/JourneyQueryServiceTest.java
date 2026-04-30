package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyQueryService 테스트")
class JourneyQueryServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @InjectMocks
    private JourneyQueryService journeyQueryService;

    @Nested
    @DisplayName("나의 여정 메인 목록 조회")
    class RetrieveMyJourneys {

        @Test
        @DisplayName("진행 중 여행과 종료된 여행을 분리해서 반환한다")
        void returnsActiveAndCompletedJourneys() {
            Long userId = 10L;

            Post activeOwnedPost = createPost(1L, userId, "제주 여행", "제주", LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
            Post activeMemberPost = createPost(2L, 20L, "부산 여행", "부산", LocalDate.now().plusDays(2), LocalDate.now().plusDays(2));
            Post completedOwnedPost = createPost(3L, userId, "도쿄 여행", "도쿄", LocalDate.now().minusDays(5), LocalDate.now().minusDays(3));
            Post completedMemberPost = createPost(4L, 30L, "서울 여행", "서울", LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));

            Participation activeApprovedParticipation = approvedParticipation(activeMemberPost, userId);
            Participation completedApprovedParticipation = approvedParticipation(completedMemberPost, userId);

            when(postRepository.findActiveJourneyPostsByOwnerId(userId, LocalDate.now()))
                    .thenReturn(List.of(activeOwnedPost));
            when(participationRepository.findActiveJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, LocalDate.now()))
                    .thenReturn(List.of(activeApprovedParticipation));
            when(postRepository.findCompletedJourneyPostsByOwnerId(userId, LocalDate.now()))
                    .thenReturn(List.of(completedOwnedPost));
            when(participationRepository.findCompletedJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, LocalDate.now()))
                    .thenReturn(List.of(completedApprovedParticipation));

            JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);

            assertThat(response.activeJourneys()).hasSize(2);
            assertThat(response.activeJourneys().get(0).postId()).isEqualTo(2L);
            assertThat(response.activeJourneys().get(0).isOwner()).isFalse();
            assertThat(response.activeJourneys().get(0).tripDurationText()).isEqualTo("당일 일정");
            assertThat(response.activeJourneys().get(1).postId()).isEqualTo(1L);
            assertThat(response.activeJourneys().get(1).isOwner()).isTrue();
            assertThat(response.activeJourneys().get(1).tripDurationText()).isEqualTo("2박 3일");

            assertThat(response.completedJourneys()).hasSize(2);
            assertThat(response.completedJourneys().get(0).postId()).isEqualTo(4L);
            assertThat(response.completedJourneys().get(0).isOwner()).isFalse();
            assertThat(response.completedJourneys().get(1).postId()).isEqualTo(3L);
            assertThat(response.completedJourneys().get(1).isOwner()).isTrue();
        }

        @Test
        @DisplayName("목록이 비어 있을 수 있다")
        void returnsEmptyList() {
            Long userId = 10L;

            when(postRepository.findActiveJourneyPostsByOwnerId(userId, LocalDate.now())).thenReturn(List.of());
            when(participationRepository.findActiveJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, LocalDate.now()))
                    .thenReturn(List.of());
            when(postRepository.findCompletedJourneyPostsByOwnerId(userId, LocalDate.now())).thenReturn(List.of());
            when(participationRepository.findCompletedJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, LocalDate.now()))
                    .thenReturn(List.of());

            JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);

            assertThat(response.activeJourneys()).isEmpty();
            assertThat(response.completedJourneys()).isEmpty();
        }
    }

    private Participation approvedParticipation(Post post, Long userId) {
        Participation participation = Participation.createParticipation(post, createUser(userId, "member-" + userId), "안녕하세요");
        ReflectionTestUtils.setField(participation, "status", ParticipationStatus.APPROVED);
        return participation;
    }

    private Post createPost(Long postId, Long ownerId, String title, String destinationCity, LocalDate startDate, LocalDate endDate) {
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
                "나의 여정 메인 목록 테스트용으로 충분히 긴 설명입니다.",
                startDate,
                endDate,
                4,
                endDate.minusDays(1),
                Gender.U,
                true,
                null,
                null,
                "https://example.com/photo.png",
                "[]",
                CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "status", PostStatus.OPEN);
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
