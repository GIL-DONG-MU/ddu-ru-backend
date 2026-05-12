package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.config.S3Properties;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.InvalidImageUrlException;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyPostAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyPostService 테스트")
class JourneyPostServiceTest {
    private static final String S3_HOST = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 12, 18, 45);

    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyMemberRepository journeyMemberRepository;
    @Mock
    private JourneyPostRepository journeyPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileImageResolver profileImageResolver;
    @Mock
    private TimeProvider timeProvider;

    private JourneyPostService journeyPostService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("dummy-bucket");
        s3Properties.setRegion("ap-northeast-2");

        journeyPostService = new JourneyPostService(
                journeyRepository,
                journeyMemberRepository,
                journeyPostRepository,
                userRepository,
                new S3ImageUrlValidator(s3Properties),
                profileImageResolver,
                timeProvider
        );
    }

    @Nested
    @DisplayName("게시글 작성")
    class Create {

        @Test
        @DisplayName("active member는 게시글을 작성할 수 있다")
        void activeMemberCanCreatePost() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            User author = createUser(userId, "author");
            JourneyPostCreateRequest request = new JourneyPostCreateRequest(
                    "  제주공항 집합  ",
                    "  출발 30분 전에 만나요.  ",
                    S3_HOST + "/journeys/posts/notice.png"
            );

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(userRepository.getByIdOrThrow(userId)).thenReturn(author);
            when(journeyPostRepository.saveAndFlush(any(JourneyPost.class))).thenAnswer(invocation -> {
                JourneyPost journeyPost = invocation.getArgument(0);
                ReflectionTestUtils.setField(journeyPost, "id", 101L);
                return journeyPost;
            });

            JourneyPostResponse response = journeyPostService.createPost(journeyId, userId, request);

            assertThat(response.journeyPostId()).isEqualTo(101L);
            assertThat(response.title()).isEqualTo("제주공항 집합");
            assertThat(response.content()).isEqualTo("출발 30분 전에 만나요.");
            assertThat(response.imageUrl()).isEqualTo(S3_HOST + "/journeys/posts/notice.png");
            assertThat(response.isAuthor()).isTrue();
            assertThat(response.author().isHost()).isTrue();

            ArgumentCaptor<JourneyPost> postCaptor = ArgumentCaptor.forClass(JourneyPost.class);
            verify(journeyPostRepository).saveAndFlush(postCaptor.capture());
            assertThat(postCaptor.getValue().getJourney()).isEqualTo(journey);
            assertThat(postCaptor.getValue().getAuthor()).isEqualTo(author);
            assertThat(postCaptor.getValue().isNotice()).isFalse();
        }

        @Test
        @DisplayName("공백 제목이면 예외가 발생한다")
        void blankTitleThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPostCreateRequest request = new JourneyPostCreateRequest(
                    "   ",
                    "내용입니다.",
                    null
            );

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(userRepository.getByIdOrThrow(userId)).thenReturn(createUser(userId, "author"));

            assertThatThrownBy(() -> journeyPostService.createPost(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyPostException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_POST_INVALID_TITLE);

            verify(journeyPostRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("허용되지 않는 이미지 URL이면 예외가 발생한다")
        void invalidImageUrlThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPostCreateRequest request = new JourneyPostCreateRequest(
                    "제주공항 집합",
                    "출발 30분 전에 만나요.",
                    "https://example.com/image.png"
            );

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(userRepository.getByIdOrThrow(userId)).thenReturn(createUser(userId, "author"));

            assertThatThrownBy(() -> journeyPostService.createPost(journeyId, userId, request))
                    .isInstanceOf(InvalidImageUrlException.class);
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class Retrieve {

        @Test
        @DisplayName("active member는 게시글 목록을 조회할 수 있다")
        void activeMemberCanRetrievePosts() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPost journeyPost = createJourneyPost(101L, journey, createUser(20L, "author"));

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(journeyPostRepository.findActivePostsByJourneyIdWithAuthorProfile(journeyId))
                    .thenReturn(List.of(journeyPost));

            JourneyPostListResponse response = journeyPostService.retrievePosts(journeyId, userId);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.posts()).hasSize(1);
            assertThat(response.posts().get(0).journeyPostId()).isEqualTo(101L);
            assertThat(response.posts().get(0).isAuthor()).isFalse();
            assertThat(response.posts().get(0).author().isHost()).isFalse();
        }

        @Test
        @DisplayName("active member가 아니면 게시글을 조회할 수 없다")
        void inactiveMemberCannotRetrievePosts() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);

            when(journeyRepository.getByIdOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyPostService.retrievePosts(journeyId, userId))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyPostRepository, never()).findActivePostsByJourneyIdWithAuthorProfile(journeyId);
        }
    }

    @Nested
    @DisplayName("게시글 수정/삭제")
    class UpdateAndDelete {

        @Test
        @DisplayName("작성자는 게시글을 수정할 수 있다")
        void authorCanUpdatePost() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long journeyPostId = 101L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(userId, "author"));
            JourneyPostUpdateRequest request = new JourneyPostUpdateRequest(
                    "수정된 제목",
                    "수정된 내용입니다.",
                    null
            );

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId))
                    .thenReturn(journeyPost);

            JourneyPostResponse response = journeyPostService.updatePost(journeyId, journeyPostId, userId, request);

            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.content()).isEqualTo("수정된 내용입니다.");
            assertThat(response.imageUrl()).isEqualTo(S3_HOST + "/journeys/posts/old.png");
            assertThat(journeyPost.getTitle()).isEqualTo("수정된 제목");
            assertThat(journeyPost.getContent()).isEqualTo("수정된 내용입니다.");
            verify(journeyPostRepository).flush();
        }

        @Test
        @DisplayName("수정 값이 모두 없으면 예외가 발생한다")
        void emptyPatchThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long journeyPostId = 101L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(userId, "author"));
            JourneyPostUpdateRequest request = new JourneyPostUpdateRequest(null, null, null);

            givenActiveMember(journeyId, userId, journey);
            givenActiveHost(journeyId, userId);
            when(journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId))
                    .thenReturn(journeyPost);

            assertThatThrownBy(() -> journeyPostService.updatePost(journeyId, journeyPostId, userId, request))
                    .isInstanceOf(InvalidJourneyPostException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_POST_EMPTY_PATCH);

            verify(journeyPostRepository, never()).flush();
        }

        @Test
        @DisplayName("작성자가 아니면 게시글을 수정할 수 없다")
        void nonAuthorCannotUpdatePost() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long journeyPostId = 101L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "author"));
            JourneyPostUpdateRequest request = new JourneyPostUpdateRequest(
                    "수정된 제목",
                    "수정된 내용입니다.",
                    null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId))
                    .thenReturn(journeyPost);

            assertThatThrownBy(() -> journeyPostService.updatePost(journeyId, journeyPostId, userId, request))
                    .isInstanceOf(JourneyPostAccessDeniedException.class);

            verify(journeyPostRepository, never()).flush();
        }

        @Test
        @DisplayName("작성자는 게시글을 soft delete 처리할 수 있다")
        void authorCanDeletePost() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long journeyPostId = 101L;
            Journey journey = createJourney(journeyId, userId);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(userId, "author"));

            givenActiveMember(journeyId, userId, journey);
            when(journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId))
                    .thenReturn(journeyPost);
            when(timeProvider.now()).thenReturn(NOW);

            journeyPostService.deletePost(journeyId, journeyPostId, userId);

            assertThat(journeyPost.isDeleted()).isTrue();
            assertThat(journeyPost.getDeletedBy()).isEqualTo(userId);
            assertThat(journeyPost.getDeletedAt()).isEqualTo(NOW);
        }
    }

    private void givenActiveMember(Long journeyId, Long userId, Journey journey) {
        when(journeyRepository.getByIdOrThrow(journeyId)).thenReturn(journey);
        when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                .thenReturn(true);
    }

    private void givenActiveHost(Long journeyId, Long hostUserId) {
        when(journeyMemberRepository.findActiveHostUserIdByJourneyId(journeyId))
                .thenReturn(Optional.of(hostUserId));
    }

    private Journey createJourney(Long journeyId, Long ownerId) {
        Post post = createPost(100L, ownerId);
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        return journey;
    }

    private JourneyPost createJourneyPost(Long journeyPostId, Journey journey, User author) {
        JourneyPost journeyPost = JourneyPost.create(
                journey,
                author,
                "기존 제목",
                "기존 내용입니다.",
                S3_HOST + "/journeys/posts/old.png"
        );
        ReflectionTestUtils.setField(journeyPost, "id", journeyPostId);
        return journeyPost;
    }

    private Post createPost(Long postId, Long ownerId) {
        User owner = createUser(ownerId, "owner-" + ownerId);
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .build();

        Post post = Post.createPost(
                owner,
                destination,
                "제주도 2박 3일 여행",
                "나의 여정 게시판 테스트용 본문입니다.",
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
        Profile profile = new Profile(user);
        ReflectionTestUtils.setField(profile, "nickname", name);
        ReflectionTestUtils.setField(user, "profile", profile);
        return user;
    }
}
