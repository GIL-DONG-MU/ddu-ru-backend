package com.dduru.gildongmu.admin.post.service;

import com.dduru.gildongmu.admin.post.dto.response.AdminPostDetailResponse;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.exception.PostNotFoundException;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPostService 테스트")
class AdminPostServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 11, 12, 0);

    @Mock
    private PostRepository postRepository;

    @Mock
    private JsonConverter jsonConverter;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private AdminPostService adminPostService;

    @Nested
    @DisplayName("게시글 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("응답에 게시글 정보가 담긴다")
        void returnsAdminPostDetailResponse() {
            Long postId = 42L;
            Post post = createPost(postId, false);

            when(postRepository.getByIdOrThrow(postId)).thenReturn(post);
            when(jsonConverter.convertJsonToList(any())).thenReturn(List.of());

            AdminPostDetailResponse response = adminPostService.getDetail(postId);

            assertThat(response.id()).isEqualTo(postId);
            assertThat(response.title()).isEqualTo("어드민 테스트 게시글");
            assertThat(response.destination()).isEqualTo("서울");
            assertThat(response.authorId()).isEqualTo(7L);
            assertThat(response.authorName()).isEqualTo("작성자");
            assertThat(response.isDeleted()).isFalse();
            verify(postRepository).getByIdOrThrow(postId);
        }

        @Test
        @DisplayName("게시글이 없으면 PostNotFoundException이 발생한다")
        void postNotFoundThrowsException() {
            Long postId = 999L;
            when(postRepository.getByIdOrThrow(postId)).thenThrow(new PostNotFoundException());

            assertThatThrownBy(() -> adminPostService.getDetail(postId))
                    .isInstanceOf(PostNotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class Delete {

        @Test
        @DisplayName("미삭제 게시글이면 softDelete가 호출된다")
        void notYetDeletedCallsSoftDelete() {
            Long postId = 1L;
            Long adminUserId = 100L;
            Post post = createPost(postId, false);

            when(postRepository.getByIdOrThrow(postId)).thenReturn(post);
            when(timeProvider.now()).thenReturn(NOW);

            adminPostService.delete(postId, adminUserId);

            assertThat(post.isDeleted()).isTrue();
            assertThat(post.getDeletedBy()).isEqualTo(adminUserId);
            verify(postRepository).getByIdOrThrow(postId);
        }

        @Test
        @DisplayName("이미 삭제된 게시글이면 softDelete를 다시 호출하지 않는다")
        void alreadyDeletedDoesNotOverwriteSoftDelete() {
            Long postId = 1L;
            Long adminUserId = 200L;
            Post post = createPost(postId, true);
            var originalDeletedAt = post.getDeletedAt();
            var originalDeletedBy = post.getDeletedBy();

            when(postRepository.getByIdOrThrow(postId)).thenReturn(post);

            adminPostService.delete(postId, adminUserId);

            assertThat(post.getDeletedAt()).isEqualTo(originalDeletedAt);
            assertThat(post.getDeletedBy()).isEqualTo(originalDeletedBy);
            verify(postRepository).getByIdOrThrow(postId);
        }
    }

    private static Post createPost(Long postId, boolean deleted) {
        User user = User.builder()
                .email("u@u.com")
                .name("작성자")
                .oauthId("kakao-7")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);

        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("서울")
                .build();
        ReflectionTestUtils.setField(destination, "id", 3L);

        Post post = Post.createPost(
                user,
                destination,
                "어드민 테스트 게시글",
                "어드민 게시글 삭제 테스트용 본문입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                5,
                LocalDate.now().plusDays(1),
                Gender.M,
                false,
                20,
                30,
                null,
                "[]",
                CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);

        if (deleted) {
            post.softDelete(55L, NOW);
        }

        return post;
    }
}
