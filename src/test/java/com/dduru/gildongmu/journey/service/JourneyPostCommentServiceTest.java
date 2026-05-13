package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostCommentException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyPostCommentAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostCommentRepository;
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
import org.springframework.data.domain.Pageable;
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
@DisplayName("JourneyPostCommentService 테스트")
class JourneyPostCommentServiceTest {
    private static final String S3_HOST = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 13, 16, 20);

    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyMemberRepository journeyMemberRepository;
    @Mock
    private JourneyPostRepository journeyPostRepository;
    @Mock
    private JourneyPostCommentRepository journeyPostCommentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileImageResolver profileImageResolver;
    @Mock
    private TimeProvider timeProvider;

    private JourneyPostCommentService journeyPostCommentService;

    @BeforeEach
    void setUp() {
        journeyPostCommentService = new JourneyPostCommentService(
                journeyRepository,
                journeyMemberRepository,
                journeyPostRepository,
                journeyPostCommentRepository,
                userRepository,
                profileImageResolver,
                timeProvider
        );
    }

    @Nested
    @DisplayName("댓글 작성")
    class Create {

        @Test
        @DisplayName("active member는 댓글을 작성할 수 있다")
        void activeMemberCanCreateComment() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            User author = createUser(userId, "member");
            JourneyPostCommentCreateRequest request = new JourneyPostCommentCreateRequest("  네 맞춰서 도착할게요!  ");

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(userRepository.getByIdOrThrow(userId)).thenReturn(author);
            when(journeyPostCommentRepository.saveAndFlush(any(JourneyPostComment.class))).thenAnswer(invocation -> {
                JourneyPostComment comment = invocation.getArgument(0);
                ReflectionTestUtils.setField(comment, "id", 11L);
                return comment;
            });

            JourneyPostCommentResponse response = journeyPostCommentService.createComment(
                    journeyId,
                    journeyPostId,
                    userId,
                    request
            );

            assertThat(response.commentId()).isEqualTo(11L);
            assertThat(response.journeyPostId()).isEqualTo(journeyPostId);
            assertThat(response.content()).isEqualTo("네 맞춰서 도착할게요!");
            assertThat(response.isAuthor()).isTrue();
            assertThat(response.author().isHost()).isFalse();

            ArgumentCaptor<JourneyPostComment> commentCaptor = ArgumentCaptor.forClass(JourneyPostComment.class);
            verify(journeyPostCommentRepository).saveAndFlush(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getJourneyPost()).isEqualTo(journeyPost);
            assertThat(commentCaptor.getValue().getAuthor()).isEqualTo(author);
            assertThat(commentCaptor.getValue().getContent()).isEqualTo("네 맞춰서 도착할게요!");
        }

        @Test
        @DisplayName("공백 댓글이면 예외가 발생한다")
        void blankContentThrowsException() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostCommentCreateRequest request = new JourneyPostCommentCreateRequest("   ");

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(userRepository.getByIdOrThrow(userId)).thenReturn(createUser(userId, "member"));

            assertThatThrownBy(() -> journeyPostCommentService.createComment(journeyId, journeyPostId, userId, request))
                    .isInstanceOf(InvalidJourneyPostCommentException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_POST_COMMENT_INVALID_CONTENT);

            verify(journeyPostCommentRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class Retrieve {

        @Test
        @DisplayName("active member는 댓글 목록을 최신순 limit 기반으로 조회할 수 있다")
        void activeMemberCanRetrieveComments() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostComment first = createComment(11L, journeyPost, createUser(30L, "first"));
            JourneyPostComment second = createComment(10L, journeyPost, createUser(userId, "me"));
            JourneyPostComment third = createComment(9L, journeyPost, createUser(40L, "third"));

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(journeyPostCommentRepository.findActiveCommentsByJourneyPostIdWithAuthorProfile(any(), any(Pageable.class)))
                    .thenReturn(List.of(first, second, third));
            when(journeyPostCommentRepository.countByJourneyPost_IdAndIsDeletedFalse(journeyPostId)).thenReturn(5L);

            JourneyPostCommentListResponse response = journeyPostCommentService.retrieveComments(
                    journeyId,
                    journeyPostId,
                    userId,
                    2
            );

            assertThat(response.journeyPostId()).isEqualTo(journeyPostId);
            assertThat(response.commentCount()).isEqualTo(5L);
            assertThat(response.hasMore()).isTrue();
            assertThat(response.comments()).hasSize(2);
            assertThat(response.comments().get(0).commentId()).isEqualTo(11L);
            assertThat(response.comments().get(1).isAuthor()).isTrue();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(journeyPostCommentRepository).findActiveCommentsByJourneyPostIdWithAuthorProfile(
                    any(),
                    pageableCaptor.capture()
            );
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("active member가 아니면 댓글 목록을 조회할 수 없다")
        void inactiveMemberCannotRetrieveComments() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);

            when(journeyRepository.getByIdOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyPostCommentService.retrieveComments(journeyId, journeyPostId, userId, 20))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyPostRepository, never()).getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        }
    }

    @Nested
    @DisplayName("댓글 수정/삭제")
    class UpdateAndDelete {

        @Test
        @DisplayName("작성자는 댓글을 수정할 수 있다")
        void authorCanUpdateComment() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long commentId = 11L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostComment comment = createComment(commentId, journeyPost, createUser(userId, "author"));
            JourneyPostCommentUpdateRequest request = new JourneyPostCommentUpdateRequest("  10분 일찍 갈게요!  ");

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(journeyPostCommentRepository.getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId))
                    .thenReturn(comment);

            JourneyPostCommentResponse response = journeyPostCommentService.updateComment(
                    journeyId,
                    journeyPostId,
                    commentId,
                    userId,
                    request
            );

            assertThat(response.content()).isEqualTo("10분 일찍 갈게요!");
            assertThat(comment.getContent()).isEqualTo("10분 일찍 갈게요!");
            verify(journeyPostCommentRepository).flush();
        }

        @Test
        @DisplayName("수정 내용이 없으면 예외가 발생한다")
        void emptyPatchThrowsException() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long commentId = 11L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostComment comment = createComment(commentId, journeyPost, createUser(userId, "author"));
            JourneyPostCommentUpdateRequest request = new JourneyPostCommentUpdateRequest(null);

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(journeyPostCommentRepository.getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId))
                    .thenReturn(comment);

            assertThatThrownBy(() -> journeyPostCommentService.updateComment(
                    journeyId,
                    journeyPostId,
                    commentId,
                    userId,
                    request
            ))
                    .isInstanceOf(InvalidJourneyPostCommentException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_POST_COMMENT_EMPTY_PATCH);

            verify(journeyPostCommentRepository, never()).flush();
        }

        @Test
        @DisplayName("작성자가 아니면 댓글을 수정할 수 없다")
        void nonAuthorCannotUpdateComment() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long commentId = 11L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostComment comment = createComment(commentId, journeyPost, createUser(30L, "author"));
            JourneyPostCommentUpdateRequest request = new JourneyPostCommentUpdateRequest("수정할게요.");

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            givenActiveHost(journeyId, 20L);
            when(journeyPostCommentRepository.getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId))
                    .thenReturn(comment);

            assertThatThrownBy(() -> journeyPostCommentService.updateComment(
                    journeyId,
                    journeyPostId,
                    commentId,
                    userId,
                    request
            ))
                    .isInstanceOf(JourneyPostCommentAccessDeniedException.class);

            verify(journeyPostCommentRepository, never()).flush();
        }

        @Test
        @DisplayName("작성자는 댓글을 soft delete 처리할 수 있다")
        void authorCanDeleteComment() {
            Long journeyId = 1L;
            Long journeyPostId = 101L;
            Long commentId = 11L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, 20L);
            JourneyPost journeyPost = createJourneyPost(journeyPostId, journey, createUser(20L, "host"));
            JourneyPostComment comment = createComment(commentId, journeyPost, createUser(userId, "author"));

            givenAccessiblePost(journeyId, journeyPostId, userId, journey, journeyPost);
            when(journeyPostCommentRepository.getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId))
                    .thenReturn(comment);
            when(timeProvider.now()).thenReturn(NOW);

            journeyPostCommentService.deleteComment(journeyId, journeyPostId, commentId, userId);

            assertThat(comment.isDeleted()).isTrue();
            assertThat(comment.getDeletedBy()).isEqualTo(userId);
            assertThat(comment.getDeletedAt()).isEqualTo(NOW);
        }
    }

    private void givenAccessiblePost(Long journeyId, Long journeyPostId, Long userId, Journey journey, JourneyPost journeyPost) {
        when(journeyRepository.getByIdOrThrow(journeyId)).thenReturn(journey);
        when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                .thenReturn(true);
        when(journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId))
                .thenReturn(journeyPost);
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
                null
        );
        ReflectionTestUtils.setField(journeyPost, "id", journeyPostId);
        return journeyPost;
    }

    private JourneyPostComment createComment(Long commentId, JourneyPost journeyPost, User author) {
        JourneyPostComment comment = JourneyPostComment.create(journeyPost, author, "기존 댓글입니다.");
        ReflectionTestUtils.setField(comment, "id", commentId);
        return comment;
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
                "댓글 테스트용 본문입니다.",
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
