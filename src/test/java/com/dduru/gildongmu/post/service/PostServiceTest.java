package com.dduru.gildongmu.post.service;

import com.dduru.gildongmu.chat.service.GroupChatRoomService;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.participation.service.ParticipationApplicantService;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPostStatusException;
import com.dduru.gildongmu.post.exception.InvalidPreferredAgeException;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.superhost.service.SuperHostService;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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

    @Spy
    private JsonConverter jsonConverter = new JsonConverter(new ObjectMapper());

    @Mock
    private GroupChatRoomService groupchatRoomService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private ProfileImageResolver profileImageResolver;

    @Mock
    private ParticipationApplicantService participationApplicantService;

    @Mock
    private SuperHostService superHostService;

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
                5,
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
        verify(groupchatRoomService).createPendingRoomForPost(savedPost, user);
        assertThat(savedPost.getRecruitDeadline()).isEqualTo(endDate.minusDays(1));
        assertThat(savedPost.isAgeAny()).isFalse();
        assertThat(savedPost.getMinAge()).isEqualTo(25);
        assertThat(savedPost.getMaxAge()).isEqualTo(35);
    }

    @DisplayName("게시글 생성 시 태그 JSON 변환은 JsonConverter에 위임되며 원본 리스트가 전달된다")
    @Test
    @SuppressWarnings("unchecked")
    void create_passesRawTagsToJsonConverter() {
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
                5,
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
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 1L);
            return post;
        });

        postService.create(userId, request);

        ArgumentCaptor<List<String>> tagCaptor = ArgumentCaptor.forClass(List.class);
        verify(jsonConverter).convertTagListToJson(tagCaptor.capture());
        assertThat(tagCaptor.getValue()).containsExactly("  제주  ", "부산");
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
                5,
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
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.create(userId, request);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.isAgeAny()).isTrue();
        assertThat(saved.getMinAge()).isNull();
        assertThat(saved.getMaxAge()).isNull();
    }

    @DisplayName("게시글 상세 조회 시 모집글 수정 권한과 표시 텍스트를 함께 응답한다")
    @Test
    void recordViewAndGetDetail_returnsDetailWithCapabilityAndDisplayText() {
        Long postId = 1L;
        Long userId = 10L;

        User owner = User.builder()
                .email("owner@a.com")
                .name("owner")
                .oauthId("kakao-10")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(owner, "id", userId);

        Profile profile = new Profile(owner);
        profile.setupInitialProfile(Gender.F, null, LocalDate.of(1998, 1, 1));
        profile.updateProfile("호스트닉", null, ProfileImageType.DEFAULT, null, "소개");
        ReflectionTestUtils.setField(owner, "profile", profile);

        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .image("https://example.com/destination.png")
                .build();

        LocalDate today = LocalDate.now();
        Post post = Post.builder()
                .user(owner)
                .destination(destination)
                .title("제주 같이 가요")
                .content("함께 떠나는 여행입니다. 충분히 긴 설명을 넣어둡니다.")
                .startDate(today.plusDays(2))
                .endDate(today.plusDays(4))
                .recruitCapacity(4)
                .recruitDeadline(today.plusDays(3))
                .preferredGender(Gender.U)
                .isAgeAny(true)
                .photoUrl("https://example.com/photo.png")
                .tags("[\"맛집\",\"바다\"]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "status", PostStatus.OPEN);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);
        when(participationApplicantService.getParticipantsForPostDetail(post)).thenReturn(List.of());
        when(participationApplicantService.getMyParticipationStatus(postId, userId, true)).thenReturn(MyParticipationStatus.NONE);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/profile.png");

        PostDetailResponse response = postService.recordViewAndGetDetail(postId, userId);

        verify(postRepository).incrementViewCount(postId);
        assertThat(response.isOwner()).isTrue();
        assertThat(response.canEditPost()).isTrue();
        assertThat(response.tripDurationText()).isEqualTo("2박 3일");
        assertThat(response.recruitDeadlineDDay()).startsWith("D-");
        assertThat(response.tags()).containsExactly("맛집", "바다");
    }

    @DisplayName("게시글 상세 조회 시 여행 시작 다음 날부터는 모집글 수정 권한이 없다")
    @Test
    void recordViewAndGetDetail_afterTravelStart_returnsCannotEditPost() {
        Long postId = 1L;
        Long userId = 10L;

        User owner = User.builder()
                .email("owner@a.com")
                .name("owner")
                .oauthId("kakao-10")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(owner, "id", userId);

        Profile profile = new Profile(owner);
        profile.setupInitialProfile(Gender.F, null, LocalDate.of(1998, 1, 1));
        profile.updateProfile("호스트닉", null, ProfileImageType.DEFAULT, null, "소개");
        ReflectionTestUtils.setField(owner, "profile", profile);

        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("서울")
                .image("https://example.com/destination.png")
                .build();

        LocalDate today = LocalDate.now();
        Post post = Post.builder()
                .user(owner)
                .destination(destination)
                .title("서울 같이 가요")
                .content("이미 여행이 시작된 뒤의 충분히 긴 설명입니다.")
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .recruitCapacity(4)
                .recruitDeadline(today.plusDays(1))
                .preferredGender(Gender.U)
                .isAgeAny(true)
                .photoUrl("https://example.com/photo.png")
                .tags("[\"야경\"]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "status", PostStatus.OPEN);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(postLikeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(false);
        when(participationApplicantService.getParticipantsForPostDetail(post)).thenReturn(List.of());
        when(participationApplicantService.getMyParticipationStatus(postId, userId, true)).thenReturn(MyParticipationStatus.NONE);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/profile.png");

        PostDetailResponse response = postService.recordViewAndGetDetail(postId, userId);

        assertThat(response.isOwner()).isTrue();
        assertThat(response.canEditPost()).isFalse();
    }

    @DisplayName("게시글 생성 시 연령 무관인데 min/max를 넣으면 예외가 발생한다")
    @Test
    void create_ageAnyWithMinMax_throws() {
        Long userId = 1L;
        PostCreateRequest request = new PostCreateRequest(
                10L,
                "제목 다섯글자이상입니다",
                "내용은 스무글자 이상이어야 합니다!!!!!!!!!!!!",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                5,
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
                5,
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
                5,
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
                .recruitCapacity(5).recruitDeadline(LocalDate.now().plusDays(1))
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

    @DisplayName("자동 마감 시 슈퍼호스트 활성 노출도 함께 취소한다")
    @Test
    void closeExpiredPosts_cancelsClosedPostExposure() {
        when(postRepository.closeExpiredPostsByDate(any(LocalDate.class))).thenReturn(2);

        int result = postService.closeExpiredPosts();

        assertThat(result).isEqualTo(2);
        verify(superHostService, times(1)).cancelActiveExposureByClosedPosts();
    }

    @DisplayName("게시글 삭제 시 슈퍼호스트 활성 노출을 취소한다")
    @Test
    void delete_cancelsSuperHostExposureForPost() {
        Long postId = 1L;
        Long userId = 10L;
        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(owner, "id", userId);
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(5).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).isAgeAny(false).minAge(20).maxAge(30)
                .photoUrl(null).tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        postService.delete(postId, userId);

        verify(superHostService).cancelActiveExposureByPostId(eq(postId));
    }

    @DisplayName("게시글을 CLOSED로 변경하면 슈퍼호스트 활성 노출을 취소한다")
    @Test
    void changeStatus_toClosed_cancelsSuperHostExposure() {
        Long postId = 1L;
        Long userId = 10L;
        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(owner, "id", userId);
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(5).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).isAgeAny(false).minAge(20).maxAge(30)
                .photoUrl(null).tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "status", PostStatus.OPEN);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        postService.changeStatus(postId, userId, new PostStatusUpdateRequest(false));

        verify(superHostService).cancelActiveExposureByPostId(eq(postId));
    }

    @DisplayName("게시글을 OPEN으로 변경하면 슈퍼호스트 노출 취소를 호출하지 않는다")
    @Test
    void changeStatus_toOpen_doesNotCancelSuperHostExposure() {
        Long postId = 1L;
        Long userId = 10L;
        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(owner, "id", userId);
        Post post = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(5).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).isAgeAny(false).minAge(20).maxAge(30)
                .photoUrl(null).tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "status", PostStatus.CLOSED);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);

        postService.changeStatus(postId, userId, new PostStatusUpdateRequest(true));

        verify(superHostService, never()).cancelActiveExposureByPostId(any());
    }

    @DisplayName("모집 상태 변경이 도메인에서 거부되면 슈퍼호스트 노출 취소를 호출하지 않는다")
    @Test
    void changeStatus_whenDomainRejects_doesNotCancelSuperHostExposure() {
        Long postId = 1L;
        Long userId = 10L;
        User owner = User.builder().email("owner@a.com").name("owner").oauthId("kakao-10").oauthType(OauthType.KAKAO).build();
        ReflectionTestUtils.setField(owner, "id", userId);
        Post built = Post.builder()
                .user(owner)
                .destination(Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build())
                .title("제목").content("내용내용내용내용내용내용내용내용")
                .startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(3))
                .recruitCapacity(5).recruitDeadline(LocalDate.now().plusDays(1))
                .preferredGender(Gender.M).isAgeAny(false).minAge(20).maxAge(30)
                .photoUrl(null).tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(built, "id", postId);
        ReflectionTestUtils.setField(built, "status", PostStatus.OPEN);
        Post post = spy(built);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        doThrow(new InvalidPostStatusException()).when(post).changeStatus(PostStatus.CLOSED);

        assertThatThrownBy(() -> postService.changeStatus(postId, userId, new PostStatusUpdateRequest(false)))
                .isInstanceOf(InvalidPostStatusException.class);

        verify(superHostService, never()).cancelActiveExposureByPostId(any());
    }
}
