package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.response.JourneyDetailResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyNotFoundException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.service.PostService;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyQueryService 테스트")
class JourneyQueryServiceTest {
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 5);

    @Mock
    private JourneyMemberRepository journeyMemberRepository;

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private PostService postService;

    @Mock
    private Clock clock;

    @InjectMocks
    private JourneyQueryService journeyQueryService;

    @BeforeEach
    void setUpClock() {
        lenient().when(clock.getZone()).thenReturn(KOREA_ZONE);
        lenient().when(clock.instant()).thenReturn(TODAY.atStartOfDay(KOREA_ZONE).toInstant());
    }

    @Nested
    @DisplayName("나의 여정 메인 목록 조회")
    class RetrieveMyJourneys {

        @Test
        @DisplayName("진행 중 여행과 종료된 여행을 분리해서 반환한다")
        void returnsActiveAndCompletedJourneys() {
            Long userId = 10L;

            Post activeOwnedPost = createPost(1L, userId, "제주 여행", "제주", TODAY.plusDays(5), TODAY.plusDays(7));
            Post activeMemberPost = createPost(2L, 20L, "부산 여행", "부산", TODAY.plusDays(2), TODAY.plusDays(2));
            Post completedOwnedPost = createPost(3L, userId, "도쿄 여행", "도쿄", TODAY.minusDays(5), TODAY.minusDays(3));
            Post completedMemberPost = createPost(4L, 30L, "서울 여행", "서울", TODAY.minusDays(3), TODAY.minusDays(1));

            JourneyMember activeOwnedJourneyMember = journeyMember(create(11L, activeOwnedPost), userId, true);
            JourneyMember activeMemberJourneyMember = journeyMember(create(12L, activeMemberPost), userId, false);
            JourneyMember completedOwnedJourneyMember = journeyMember(create(13L, completedOwnedPost), userId, true);
            JourneyMember completedMemberJourneyMember = journeyMember(create(14L, completedMemberPost), userId, false);

            when(journeyMemberRepository.findActiveJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, TODAY))
                    .thenReturn(List.of(activeOwnedJourneyMember, activeMemberJourneyMember));
            when(journeyMemberRepository.findCompletedJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, TODAY))
                    .thenReturn(List.of(completedOwnedJourneyMember, completedMemberJourneyMember));

            JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);

            assertThat(response.activeJourneys()).hasSize(2);
            assertThat(response.activeJourneys().get(0).journeyId()).isEqualTo(12L);
            assertThat(response.activeJourneys().get(0).isOwner()).isFalse();
            assertThat(response.activeJourneys().get(0).tripDurationText()).isEqualTo("당일치기");
            assertThat(response.activeJourneys().get(0).recruitDeadlineDDay()).isEqualTo("D-1");
            assertThat(response.activeJourneys().get(1).journeyId()).isEqualTo(11L);
            assertThat(response.activeJourneys().get(1).isOwner()).isTrue();
            assertThat(response.activeJourneys().get(1).tripDurationText()).isEqualTo("2박 3일");
            assertThat(response.activeJourneys().get(1).recruitDeadlineDDay()).isEqualTo("D-6");

            assertThat(response.completedJourneys()).hasSize(2);
            assertThat(response.completedJourneys().get(0).journeyId()).isEqualTo(14L);
            assertThat(response.completedJourneys().get(0).isOwner()).isFalse();
            assertThat(response.completedJourneys().get(0).recruitDeadlineDDay()).isEqualTo("마감");
            assertThat(response.completedJourneys().get(1).journeyId()).isEqualTo(13L);
            assertThat(response.completedJourneys().get(1).isOwner()).isTrue();
            assertThat(response.completedJourneys().get(1).recruitDeadlineDDay()).isEqualTo("마감");
        }

        @Test
        @DisplayName("목록이 비어 있을 수 있다")
        void returnsEmptyList() {
            Long userId = 10L;

            when(journeyMemberRepository.findActiveJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, TODAY))
                    .thenReturn(List.of());
            when(journeyMemberRepository.findCompletedJourneyMembersByUserIdAndStatus(userId, JourneyMemberStatus.ACTIVE, TODAY))
                    .thenReturn(List.of());

            JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);

            assertThat(response.activeJourneys()).isEmpty();
            assertThat(response.completedJourneys()).isEmpty();
        }
    }

    @Nested
    @DisplayName("나의 여정 상세 조회")
    class RetrieveMyJourneyDetail {

        @Test
        @DisplayName("active journey member면 그룹 채팅방 ID와 상세 응답을 함께 반환한다")
        void returnsJourneyDetailForActiveMember() {
            Long journeyId = 100L;
            Long userId = 10L;
            Long roomId = 55L;
            Long postId = 200L;

            Journey journey = create(journeyId, createPost(postId, 1L, "제주도 2박 3일 여행", "제주", LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)));
            PostDetailResponse postDetail = createPostDetailResponse(postId);
            ChatRoom groupRoom = ChatRoom.builder()
                    .roomType(ChatRoomType.GROUP)
                    .maxCapacity(4)
                    .build();
            ReflectionTestUtils.setField(groupRoom, "id", roomId);

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(true);
            when(postService.getDetail(journey.getPost(), userId)).thenReturn(postDetail);
            when(chatRoomRepository.findByPostIdAndRoomType(postId, ChatRoomType.GROUP))
                    .thenReturn(Optional.of(groupRoom));

            JourneyDetailResponse response = journeyQueryService.retrieveMyJourneyDetail(journeyId, userId);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.groupRoomId()).isEqualTo(roomId);
            assertThat(response.title()).isEqualTo("제주도 2박 3일 여행");
            assertThat(response.photoUrl()).isEqualTo("https://example.com/photo.png");
            assertThat(response.recruitDeadlineDDay()).isEqualTo("D-3");
            assertThat(response.recruitCount()).isEqualTo(2);
            assertThat(response.recruitCapacity()).isEqualTo(4);
            assertThat(response.isOwner()).isTrue();
        }

        @Test
        @DisplayName("active journey member가 아니면 접근 예외가 발생한다")
        void throwsWhenUserIsNotActiveJourneyMember() {
            Long journeyId = 100L;
            Long userId = 10L;

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId))
                    .thenReturn(create(journeyId, createPost(200L, 1L, "제주도 2박 3일 여행", "제주", LocalDate.now().plusDays(5), LocalDate.now().plusDays(7))));
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyQueryService.retrieveMyJourneyDetail(journeyId, userId))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(postService, never()).getDetail(any(Post.class), anyLong());
            verify(chatRoomRepository, never()).findByPostIdAndRoomType(200L, ChatRoomType.GROUP);
        }

        @Test
        @DisplayName("존재하지 않는 여정이면 not found 예외가 발생한다")
        void throwsWhenJourneyDoesNotExist() {
            Long journeyId = 999L;
            Long userId = 10L;

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId))
                    .thenThrow(new JourneyNotFoundException());

            assertThatThrownBy(() -> journeyQueryService.retrieveMyJourneyDetail(journeyId, userId))
                    .isInstanceOf(JourneyNotFoundException.class);

            verify(journeyMemberRepository, never()).existsByJourneyIdAndUserIdAndStatus(anyLong(), anyLong(), any());
            verify(postService, never()).getDetail(anyLong(), anyLong());
            verify(postService, never()).getDetail(any(Post.class), anyLong());
            verify(chatRoomRepository, never()).findByPostIdAndRoomType(anyLong(), any());
        }
    }

    private JourneyMember journeyMember(Journey journey, Long userId, boolean isHost) {
        JourneyMember journeyMember = isHost
                ? JourneyMember.createHost(journey, createUser(userId, "host-" + userId))
                : JourneyMember.createMember(journey, createUser(userId, "member-" + userId));
        ReflectionTestUtils.setField(journeyMember, "status", JourneyMemberStatus.ACTIVE);
        return journeyMember;
    }

    private Journey create(Long journeyId, Post post) {
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        return journey;
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

    private PostDetailResponse createPostDetailResponse(Long postId) {
        return new PostDetailResponse(
                postId,
                "제주도 2박 3일 여행",
                "나의 여정 상세 조회 테스트용 본문입니다.",
                PostStatus.OPEN,
                false,
                3,
                5,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                "제주",
                4,
                2,
                LocalDate.now().plusDays(3),
                null,
                true,
                null,
                null,
                "https://example.com/photo.png",
                List.of("맛집", "바다"),
                0,
                0,
                LocalDateTime.now(),
                null,
                true,
                true,
                false,
                List.of(),
                MyParticipationStatus.NONE,
                "2박 3일",
                "D-3"
        );
    }
}
