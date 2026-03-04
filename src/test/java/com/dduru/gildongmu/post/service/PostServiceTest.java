package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidRecruitSettingsException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    @InjectMocks
    private PostService postService;

    @DisplayName("게시글 생성 시 유효한 요청이면 생성 후 ID를 반환한다")
    @Test
    void create_validRequest_returnsPostId() throws Exception {
        Long userId = 1L;
        Long destinationId = 10L;
        User user = User.builder().email("a@a.com").name("user").oauthId("kakao-1").oauthType(OauthType.KAKAO).build();
        setEntityId(user, 1L);
        Destination destination = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        setEntityId(destination, destinationId);

        PostCreateRequest request = new PostCreateRequest(
                destinationId,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                LocalDate.now().plusDays(5),
                Gender.M,
                List.of(AgeRange.AGE_20s, AgeRange.AGE_30s),
                null,
                List.of("태그1"),
                RecruitType.PUBLIC,
                RecruitMethod.ALWAYS,
                CompanionType.FULL
        );

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(destinationRepository.getByIdOrThrow(destinationId)).thenReturn(destination);
        when(jsonConverter.convertListToJson(anyList())).thenReturn("[]");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            setEntityId(post, 100L);
            return post;
        });

        PostCreateResponse response = postService.create(userId, request);

        assertThat(response.id()).isEqualTo(100L);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo(request.title());
        assertThat(savedPost.getRecruitType()).isEqualTo(RecruitType.PUBLIC);
        assertThat(savedPost.getCompanionType()).isEqualTo(CompanionType.FULL);
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
                null,
                Gender.M,
                List.of(AgeRange.AGE_20s),
                null,
                null,
                RecruitType.PRIVATE,
                RecruitMethod.ALWAYS,
                null
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidPostDateException.class)
                .hasMessageContaining("종료일");
    }

    @DisplayName("게시글 생성 시 공개 모집인데 동행 방식을 선택하지 않으면 예외가 발생한다")
    @Test
    void create_publicRecruitWithoutCompanionType_throwsInvalidRecruitSettingsException() {
        Long userId = 1L;
        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                null,
                Gender.M,
                List.of(AgeRange.AGE_20s),
                null,
                null,
                RecruitType.PUBLIC,
                RecruitMethod.ALWAYS,
                null
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidRecruitSettingsException.class);
    }

    @DisplayName("게시글 생성 시 기간설정 모집인데 모집 마감일이 없으면 예외가 발생한다")
    @Test
    void create_periodRecruitWithoutDeadline_throwsInvalidPostDateException() {
        Long userId = 1L;
        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                null,
                Gender.M,
                List.of(AgeRange.AGE_20s),
                null,
                null,
                RecruitType.PRIVATE,
                RecruitMethod.PERIOD,
                null
        );

        assertThatThrownBy(() -> postService.create(userId, request))
                .isInstanceOf(InvalidPostDateException.class);
    }

    @DisplayName("게시글 수정 시 작성자가 아니면 예외가 발생한다")
    @Test
    void update_notOwner_throwsPostAccessDeniedException() throws Exception {
        Long postId = 1L;
        Long ownerId = 10L;
        Long requesterId = 99L;

        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        setEntityId(owner, ownerId);
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(4).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).preferredAges(List.of(AgeRange.AGE_20s))
                .photoUrls("[]").tags("[]")
                .recruitType(RecruitType.PRIVATE).recruitMethod(RecruitMethod.ALWAYS).companionType(null)
                .build();
        setEntityId(post, postId);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> postService.update(postId, requesterId, updateRequest))
                .isInstanceOf(PostAccessDeniedException.class);
    }

    private static void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
