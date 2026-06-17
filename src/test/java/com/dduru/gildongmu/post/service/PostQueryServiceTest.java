package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostSortType;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.superhost.service.SuperHostService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostQueryService 테스트")
class PostQueryServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 5);

    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private ProfileImageResolver profileImageResolver;
    @Mock private SuperHostService superHostService;
    @Mock private TimeProvider timeProvider;

    @InjectMocks
    private PostQueryService postQueryService;

    @BeforeEach
    void setUp() {
        when(timeProvider.today()).thenReturn(TODAY);
        lenient().when(profileImageResolver.resolve(any(Profile.class))).thenReturn(null);
        lenient().when(superHostService.findActiveSuperHostExposures(any())).thenReturn(Map.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // hasLiked
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasLiked")
    class HasLiked {

        @Test
        @DisplayName("비로그인 사용자는 hasLiked가 항상 false이고 좋아요 조회를 하지 않는다")
        void nonLoggedInUserHasLikedAlwaysFalse() {
            Post post = createPost(1L);
            PostListRequest request = listRequest(2, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(post));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.posts()).hasSize(1);
            assertThat(response.posts().get(0).hasLiked()).isFalse();
            verify(postLikeRepository, never()).findLikedPostIdsByUserId(any(), any());
        }

        @Test
        @DisplayName("로그인 사용자는 좋아요한 게시글의 hasLiked가 true다")
        void loggedInUserHasLikedTrueForLikedPost() {
            Post likedPost = createPost(1L);
            Post notLikedPost = createPost(2L);
            Long userId = 10L;
            PostListRequest request = listRequest(5, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(likedPost, notLikedPost));
            when(postLikeRepository.findLikedPostIdsByUserId(eq(userId), any()))
                    .thenReturn(Set.of(1L));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, userId);

            assertThat(response.posts()).hasSize(2);
            assertThat(response.posts().get(0).hasLiked()).isTrue();
            assertThat(response.posts().get(1).hasLiked()).isFalse();
        }

        @Test
        @DisplayName("로그인 사용자여도 게시글이 없으면 좋아요 조회를 하지 않는다")
        void emptyPostsSkipsLikeQuery() {
            Long userId = 10L;
            PostListRequest request = listRequest(5, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of());

            postQueryService.retrieveAllWithFilter(request, userId);

            verify(postLikeRepository, never()).findLikedPostIdsByUserId(any(), any());
            verify(superHostService, never()).findActiveSuperHostExposures(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 페이지네이션
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @Test
        @DisplayName("조회 결과가 size보다 많으면 hasNext가 true이고 size개만 반환된다")
        void hasNextTrueWhenMoreThanSize() {
            int size = 2;
            PostListRequest request = listRequest(size, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(createPost(1L), createPost(2L), createPost(3L)));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.posts()).hasSize(size);
        }

        @Test
        @DisplayName("조회 결과가 size 이하이면 hasNext가 false다")
        void hasNextFalseWhenLessThanOrEqualSize() {
            int size = 2;
            PostListRequest request = listRequest(size, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(createPost(1L), createPost(2L)));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.posts()).hasSize(2);
        }

        @Test
        @DisplayName("LATEST 정렬에서 hasNext가 true이면 nextCursor는 마지막 게시글의 ID이고 nextCursorValue는 null이다")
        void nextCursorIsLastPostIdWhenHasNext() {
            int size = 2;
            PostListRequest request = listRequest(size, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(createPost(10L), createPost(5L), createPost(1L)));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(5L);
            assertThat(response.nextCursorValue()).isNull();
        }

        @Test
        @DisplayName("VIEW 정렬에서 hasNext가 true이면 nextCursorValue는 마지막 게시글의 viewCount다")
        void nextCursorValueIsLastPostViewCountForViewSort() {
            int size = 2;
            PostListRequest request = listRequestWithSort(size, null, null, PostSortType.VIEW);
            Post post1 = createPost(10L);
            Post post2 = createPost(5L);
            Post post3 = createPost(1L);
            ReflectionTestUtils.setField(post1, "viewCount", 100);
            ReflectionTestUtils.setField(post2, "viewCount", 50);
            ReflectionTestUtils.setField(post3, "viewCount", 20);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(post1, post2, post3));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(5L);
            assertThat(response.nextCursorValue()).isEqualTo(50);
        }

        @Test
        @DisplayName("LIKE 정렬에서 hasNext가 true이면 nextCursorValue는 마지막 게시글의 likeCount다")
        void nextCursorValueIsLastPostLikeCountForLikeSort() {
            int size = 2;
            PostListRequest request = listRequestWithSort(size, null, null, PostSortType.LIKE);
            Post post1 = createPost(10L);
            Post post2 = createPost(5L);
            Post post3 = createPost(1L);
            ReflectionTestUtils.setField(post1, "likeCount", 30);
            ReflectionTestUtils.setField(post2, "likeCount", 15);
            ReflectionTestUtils.setField(post3, "likeCount", 5);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(post1, post2, post3));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(5L);
            assertThat(response.nextCursorValue()).isEqualTo(15);
        }

        @Test
        @DisplayName("hasNext가 false이면 nextCursorValue는 항상 null이다")
        void nextCursorValueIsNullWhenNoNextPage() {
            int size = 2;
            PostListRequest request = listRequestWithSort(size, null, null, PostSortType.VIEW);
            Post post1 = createPost(10L);
            ReflectionTestUtils.setField(post1, "viewCount", 100);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of(post1));

            PostListResponse response = postQueryService.retrieveAllWithFilter(request, null);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursorValue()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 커서 조회
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("커서 조회")
    class Cursor {

        @Test
        @DisplayName("VIEW 정렬에서 cursor와 cursorValue가 있으면 cursorValue를 레포지토리에 전달한다")
        void viewSortWithCursorPassesCursorValue() {
            Long cursorId = 100L;
            Integer cursorValue = 150;
            PostListRequest request = listRequestWithSort(5, cursorId, cursorValue, PostSortType.VIEW);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), eq(cursorValue), any(Pageable.class)))
                    .thenReturn(List.of());

            postQueryService.retrieveAllWithFilter(request, null);

            verify(postRepository, never()).findById(any());
            verify(postRepository).findPostsWithFilters(any(), any(LocalDate.class), eq(cursorValue), any(Pageable.class));
        }

        @Test
        @DisplayName("LATEST 정렬에서는 cursor가 있어도 cursorValue를 사용하지 않는다")
        void latestSortWithCursorPassesNullCursorValue() {
            PostListRequest request = listRequest(5, 100L);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of());

            postQueryService.retrieveAllWithFilter(request, null);

            verify(postRepository, never()).findById(any());
            verify(postRepository).findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("cursor가 없으면 cursorValue 없이 조회한다")
        void withoutCursorPassesNullCursorValue() {
            PostListRequest request = listRequest(5, null);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of());

            postQueryService.retrieveAllWithFilter(request, null);

            verify(postRepository, never()).findById(any());
            verify(postRepository).findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("VIEW 정렬에서 cursorValue가 없으면 null을 레포지토리에 전달한다")
        void viewSortWithoutCursorValuePassesNull() {
            PostListRequest request = listRequestWithSort(5, null, null, PostSortType.VIEW);

            when(postRepository.findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class)))
                    .thenReturn(List.of());

            postQueryService.retrieveAllWithFilter(request, null);

            verify(postRepository, never()).findById(any());
            verify(postRepository).findPostsWithFilters(any(), any(LocalDate.class), isNull(), any(Pageable.class));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────────────────────────────────────────

    private PostListRequest listRequest(int size, Long cursor) {
        return new PostListRequest(cursor, null, size, null, null, null, null, null, null, null, null, null, PostSortType.LATEST);
    }

    private PostListRequest listRequestWithSort(int size, Long cursor, Integer cursorValue, PostSortType sort) {
        return new PostListRequest(cursor, cursorValue, size, null, null, null, null, null, null, null, null, null, sort);
    }

    private Post createPost(Long postId) {
        User user = User.builder()
                .email("user" + postId + "@a.com")
                .name("user" + postId)
                .oauthId("oauth-" + postId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", postId);

        Profile profile = new Profile(user);
        profile.setupInitialProfile(Gender.M, null, LocalDate.of(1995, 1, 1));
        profile.updateProfile("닉네임" + postId, null, ProfileImageType.DEFAULT, null, "소개");
        ReflectionTestUtils.setField(user, "profile", profile);

        Destination destination = Destination.builder()
                .countryCode("KR").countryName("대한민국").city("서울").build();

        LocalDate start = TODAY.plusDays(1);
        LocalDate end = TODAY.plusDays(3);
        Post post = Post.createPost(
                user, destination,
                "서울 여행 같이 가실 분 모집합니다",
                "함께 서울 여행할 동행자를 모집합니다. 편하게 신청해주세요.",
                start, end, 3, end.minusDays(1),
                Gender.U, true, null, null, null, "[]", CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
