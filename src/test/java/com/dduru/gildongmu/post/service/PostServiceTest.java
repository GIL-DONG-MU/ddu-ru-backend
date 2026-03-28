package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPreferredAgeException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.participation.service.ParticipationService;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("PostService 테스트")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private JsonConverter jsonConverter;

    @Mock
    private ParticipationService participationService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private ProfileImageResolver profileImageResolver;

    @InjectMocks
    private PostService postService;

    @DisplayName("게시글 생성 시 유효한 요청이면 생성 후 ID를 반환하고 모집 마감일은 여행 종료일 전날이다")
    @Test
    void create_validRequest_returnsPostId() {
        Long userId = 1L;
        Long destinationId = 10L;
        User user = User.builder().email("a@a.com").name("user").oauthId("kakao-1").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Destination destination = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        ReflectionTestUtils.setField(destination, "id", destinationId);

        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(12);

        PostCreateRequest request = new PostCreateRequest(
                destinationId,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                startDate,
                endDate,
                4,
                Gender.M,
                false,
                25,
                35,
                null,
                List.of("태그1"),
                CompanionType.FULL
        );

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(destinationRepository.getByIdOrThrow(destinationId)).thenReturn(destination);
        when(jsonConverter.convertListToJson(any())).thenReturn("[]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 100L);
            return post;
        });

        PostCreateResponse response = postService.create(userId, request);

        assertThat(response.id()).isEqualTo(100L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo(request.title());
        assertThat(savedPost.getCompanionType()).isEqualTo(CompanionType.FULL);
        assertThat(savedPost.getRecruitDeadline()).isEqualTo(endDate.minusDays(1));
        assertThat(savedPost.isAgeAny()).isFalse();
        assertThat(savedPost.getMinAge()).isEqualTo(25);
        assertThat(savedPost.getMaxAge()).isEqualTo(35);
    }

    @DisplayName("게시글 생성 시 태그 앞뒤 공백은 JSON 직렬화 전에 제거된다")
    @Test
    @SuppressWarnings("unchecked")
    void create_stripsWhitespaceFromTagsBeforeJson() {
        Long userId = 1L;
        Long destinationId = 10L;
        User user = User.builder().email("a@a.com").name("user").oauthId("kakao-1").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Destination destination = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        ReflectionTestUtils.setField(destination, "id", destinationId);

        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(12);

        PostCreateRequest request = new PostCreateRequest(
                destinationId,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                startDate,
                endDate,
                4,
                Gender.M,
                false,
                25,
                35,
                null,
                List.of("  제주  ", "부산"),
                CompanionType.FULL
        );

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(destinationRepository.getByIdOrThrow(destinationId)).thenReturn(destination);
        when(jsonConverter.convertListToJson(any())).thenReturn("[\"제주\",\"부산\"]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 1L);
            return post;
        });

        postService.create(userId, request);

        ArgumentCaptor<List<String>> tagCaptor = ArgumentCaptor.forClass(List.class);
        verify(jsonConverter).convertListToJson(tagCaptor.capture());
        assertThat(tagCaptor.getValue()).containsExactly("제주", "부산");
    }

    @DisplayName("게시글 생성 시 연령 무관이면 isAgeAny이 true이고 min/max는 null이다")
    @Test
    void create_ageAny_savesFlagAndNullMinMax() {
        Long userId = 1L;
        Long destinationId = 10L;
        User user = User.builder().email("a@a.com").name("user").oauthId("kakao-1").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Destination destination = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        ReflectionTestUtils.setField(destination, "id", destinationId);

        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(12);

        PostCreateRequest request = new PostCreateRequest(
                destinationId,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                startDate,
                endDate,
                4,
                Gender.M,
                true,
                null,
                null,
                null,
                List.of(),
                CompanionType.FULL
        );

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(destinationRepository.getByIdOrThrow(destinationId)).thenReturn(destination);
        when(jsonConverter.convertListToJson(any())).thenReturn("[]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.create(userId, request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.isAgeAny()).isTrue();
        assertThat(saved.getMinAge()).isNull();
        assertThat(saved.getMaxAge()).isNull();
    }

    @DisplayName("게시글 생성 시 연령 무관인데 min/max를내면 예외가 발생한다")
    @Test
    void create_ageAnyWithMinMax_throws() {
        Long userId = 1L;
        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                Gender.M,
                true,
                20,
                30,
                null,
                null,
                CompanionType.FULL
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidPreferredAgeException.class);
    }

    @DisplayName("게시글 생성 시 종료일이 시작일보다 이전이면 예외가 발생한다")
    @Test
    void create_endBeforeStart_throwsInvalidPostDateException() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(3);

        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                startDate,
                endDate,
                4,
                Gender.M,
                true,
                null,
                null,
                null,
                null,
                CompanionType.FULL
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidPostDateException.class)
                .hasMessageContaining("종료일");
    }

    @DisplayName("게시글 생성 시 구간을 쓰는데 min만 있으면 예외가 발생한다")
    @Test
    void create_onlyMinAge_throwsInvalidPreferredAgeException() {
        Long userId = 1L;
        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                Gender.M,
                false,
                25,
                null,
                null,
                null,
                CompanionType.FULL
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidPreferredAgeException.class);
    }

    @DisplayName("게시글 수정 시 작성자가 아니면 예외가 발생한다")
    @Test
    void update_notOwner_throwsPostAccessDeniedException() {
        Long postId = 1L;
        Long ownerId = 10L;
        Long requesterId = 99L;

        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(4).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).isAgeAny(false).minAge(20).maxAge(30)
                .photoUrl(null).tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> postService.update(postId, requesterId, updateRequest))
                .isInstanceOf(PostAccessDeniedException.class);
    }
}
