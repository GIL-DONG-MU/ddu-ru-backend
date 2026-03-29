package com.dduru.gildongmu.admin.post.service;

import com.dduru.gildongmu.admin.post.dto.response.AdminPostDetailResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPostService 테스트")
class AdminPostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private JsonConverter jsonConverter;

    @InjectMocks
    private AdminPostService adminPostService;

    @DisplayName("관리자 게시글 상세 조회 시 응답에 게시글 정보가 담긴다")
    @Test
    void getDetail_returnsAdminPostDetailResponse() {
        Long postId = 42L;
        Post post = createPost(postId, false);

        when(postRepository.getByIdOrThrow(postId)).thenReturn(post);
        when(jsonConverter.convertJsonToList(any())).thenReturn(List.of());

        AdminPostDetailResponse response = adminPostService.getDetail(postId);

        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.destination()).isEqualTo("서울");
        assertThat(response.authorId()).isEqualTo(7L);
        assertThat(response.authorName()).isEqualTo("작성자");
        assertThat(response.isDeleted()).isFalse();
        verify(postRepository).getByIdOrThrow(postId);
    }

    @DisplayName("관리자 게시글 상세 조회 시 게시글이 없으면 PostNotFoundException이 발생한다")
    @Test
    void getDetail_postNotFound_throwsPostNotFoundException() {
        Long postId = 999L;
        when(postRepository.getByIdOrThrow(postId)).thenThrow(PostNotFoundException.of(postId));

        assertThatThrownBy(() -> adminPostService.getDetail(postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @DisplayName("관리자 게시글 삭제 시 미삭제 게시글이면 softDelete가 호출된다")
    @Test
    void delete_notYetDeleted_callsSoftDelete() {
        Long postId = 1L;
        Long adminUserId = 100L;
        Post post = createPost(postId, false);

        when(postRepository.getByIdOrThrow(postId)).thenReturn(post);

        adminPostService.delete(postId, adminUserId);

        assertThat(post.isDeleted()).isTrue();
        assertThat(post.getDeletedBy()).isEqualTo(adminUserId);
        verify(postRepository).getByIdOrThrow(postId);
    }

    @DisplayName("관리자 게시글 삭제 시 이미 삭제된 게시글이면 softDelete를 다시 호출하지 않는다")
    @Test
    void delete_alreadyDeleted_doesNotOverwriteSoftDelete() {
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

        Post post = Post.builder()
                .user(user)
                .destination(destination)
                .title("제목")
                .content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(5)
                .recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M)
                .isAgeAny(false)
                .photoUrl(null)
                .tags("[]")
                .minAge(20)
                .maxAge(30)
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        if (deleted) {
            post.softDelete(55L);
        }

        return post;
    }
}
