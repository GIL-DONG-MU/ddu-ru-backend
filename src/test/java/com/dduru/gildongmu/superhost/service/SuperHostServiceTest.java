package com.dduru.gildongmu.superhost.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.superhost.domain.SuperHostExposure;
import com.dduru.gildongmu.superhost.domain.SuperHostTicket;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostExposureStatus;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketSource;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketStatus;
import com.dduru.gildongmu.superhost.dto.response.MySuperHostStatusResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostApplyResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostPostListResponse;
import com.dduru.gildongmu.superhost.exception.SuperHostAlreadyActiveException;
import com.dduru.gildongmu.superhost.exception.SuperHostPostNotApplicableException;
import com.dduru.gildongmu.superhost.repository.SuperHostExposureRepository;
import com.dduru.gildongmu.superhost.repository.SuperHostTicketRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperHostService 테스트")
class SuperHostServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private SuperHostTicketRepository superHostTicketRepository;
    @Mock
    private SuperHostExposureRepository superHostExposureRepository;
    @Mock
    private ProfileImageResolver profileImageResolver;

    @InjectMocks
    private SuperHostService superHostService;

    @Test
    @DisplayName("온보딩 보상 티켓은 사용자당 1회만 지급된다")
    void grantOnboardingRewardTicket_issuesOnlyOnce() {
        Long userId = 1L;
        User user = createUser(userId);

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);

        superHostService.grantOnboardingRewardTicket(userId);

        ArgumentCaptor<SuperHostTicket> ticketCaptor = ArgumentCaptor.forClass(SuperHostTicket.class);
        verify(superHostTicketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getSource()).isEqualTo(SuperHostTicketSource.ONBOARDING_SURVEY);
        assertThat(ticketCaptor.getValue().getStatus()).isEqualTo(SuperHostTicketStatus.UNUSED);
    }

    @Test
    @DisplayName("온보딩 보상 티켓이 이미 있으면 예외 없이 종료한다")
    void grantOnboardingRewardTicket_ignoresDuplicateInsert() {
        Long userId = 1L;
        User user = createUser(userId);
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "duplicate",
                new DuplicateKeyException("Duplicate entry for key 'uk_super_host_tickets_user_source'")
        );

        when(userRepository.getByIdOrThrow(userId)).thenReturn(user);
        when(superHostTicketRepository.save(any(SuperHostTicket.class))).thenThrow(exception);

        assertThatCode(() -> superHostService.grantOnboardingRewardTicket(userId))
                .doesNotThrowAnyException();

        verify(userRepository).getByIdOrThrow(userId);
        verify(superHostTicketRepository).save(any(SuperHostTicket.class));
    }

    @Test
    @DisplayName("티켓 적용 성공 시 노출이 ACTIVE로 생성된다")
    void applyTicketToPost_success() {
        Long userId = 1L;
        Long postId = 10L;
        User user = createUser(userId);
        Post post = createPost(postId, user);
        SuperHostTicket ticket = SuperHostTicket.create(user, SuperHostTicketSource.ONBOARDING_SURVEY, 3);
        ReflectionTestUtils.setField(ticket, "id", 99L);

        when(postRepository.findSuperHostApplicableByIdAndUserId(postId, userId, PostStatus.OPEN))
                .thenReturn(Optional.of(post));
        when(superHostExposureRepository.existsByUser_IdAndStatusAndEndedAtAfter(eq(userId), eq(SuperHostExposureStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(false);
        when(superHostTicketRepository.findFirstByUser_IdAndStatusOrderByIdAsc(userId, SuperHostTicketStatus.UNUSED))
                .thenReturn(Optional.of(ticket));
        when(superHostTicketRepository.countByUser_IdAndStatus(userId, SuperHostTicketStatus.UNUSED)).thenReturn(0L);

        SuperHostApplyResponse response = superHostService.applyTicketToPost(userId, postId);

        ArgumentCaptor<SuperHostExposure> exposureCaptor = ArgumentCaptor.forClass(SuperHostExposure.class);
        verify(superHostExposureRepository).save(exposureCaptor.capture());
        assertThat(exposureCaptor.getValue().getStatus()).isEqualTo(SuperHostExposureStatus.ACTIVE);
        assertThat(ticket.getStatus()).isEqualTo(SuperHostTicketStatus.USED);
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.ticketId()).isEqualTo(99L);
        assertThat(response.startedAt()).isNotNull();
        assertThat(response.endsAt()).isNotNull();
        assertThat(response.unusedTicketCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("활성 슈퍼호스트가 이미 있으면 티켓 적용이 실패한다")
    void applyTicketToPost_failsWhenAlreadyActive() {
        Long userId = 1L;
        User user = createUser(userId);
        Post post = createPost(10L, user);
        when(postRepository.findSuperHostApplicableByIdAndUserId(10L, userId, PostStatus.OPEN))
                .thenReturn(Optional.of(post));
        when(superHostExposureRepository.existsByUser_IdAndStatusAndEndedAtAfter(eq(userId), eq(SuperHostExposureStatus.ACTIVE), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> superHostService.applyTicketToPost(userId, 10L))
                .isInstanceOf(SuperHostAlreadyActiveException.class);
    }

    @Test
    @DisplayName("슈퍼호스트 적용 불가 게시글이면 예외가 발생한다")
    void applyTicketToPost_failsWhenPostNotApplicable() {
        when(postRepository.findSuperHostApplicableByIdAndUserId(10L, 1L, PostStatus.OPEN))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> superHostService.applyTicketToPost(1L, 10L))
                .isInstanceOf(SuperHostPostNotApplicableException.class);
    }

    @Test
    @DisplayName("슈퍼호스트 목록은 ACTIVE 노출로 구성된다")
    void retrieveSuperHostPosts_returnsActiveList() {
        User user = createUser(1L);
        Post post = createPost(10L, user);
        SuperHostTicket ticket = SuperHostTicket.create(user, SuperHostTicketSource.ONBOARDING_SURVEY, 3);
        LocalDateTime endsAt = LocalDateTime.now().plusDays(3);
        SuperHostExposure exposure = SuperHostExposure.create(ticket, user, post, LocalDateTime.now(), endsAt);

        when(superHostExposureRepository.findVisibleExposures(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(exposure));
        when(profileImageResolver.resolve(any(Profile.class))).thenReturn("https://example.com/profile.png");

        SuperHostPostListResponse response = superHostService.retrieveSuperHostPosts(5);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo(10L);
        assertThat(response.items().get(0).isSuperHost()).isTrue();
        assertThat(response.items().get(0).superHostEndsAt()).isEqualTo(endsAt);
    }

    @Test
    @DisplayName("만료 노출 종료 처리는 ACTIVE -> ENDED 갱신 수를 반환한다")
    void endExpiredExposures_returnsUpdatedCount() {
        when(superHostExposureRepository.endExpiredExposures(
                eq(SuperHostExposureStatus.ACTIVE),
                eq(SuperHostExposureStatus.ENDED),
                any(LocalDateTime.class)
        )).thenReturn(2);

        int result = superHostService.endExpiredExposures();

        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("내 슈퍼호스트 현황 조회 시 티켓 수와 활성 정보를 반환한다")
    void getMyStatus_returnsIssuedCountAndActiveExposure() {
        Long userId = 1L;
        User user = createUser(userId);
        Post post = createPost(10L, user);
        SuperHostTicket ticket = SuperHostTicket.create(user, SuperHostTicketSource.ONBOARDING_SURVEY, 3);
        SuperHostExposure exposure = SuperHostExposure.create(ticket, user, post, LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        when(superHostTicketRepository.countByUser_IdAndStatus(userId, SuperHostTicketStatus.UNUSED)).thenReturn(2L);
        when(superHostExposureRepository.findFirstByUser_IdAndStatusAndEndedAtAfterOrderByStartedAtDesc(
                eq(userId), eq(SuperHostExposureStatus.ACTIVE), any(LocalDateTime.class))
        ).thenReturn(Optional.of(exposure));

        MySuperHostStatusResponse response = superHostService.getMyStatus(userId);

        assertThat(response.unusedTicketCount()).isEqualTo(2);
        assertThat(response.hasActive()).isTrue();
        assertThat(response.activePostId()).isEqualTo(10L);
        assertThat(response.activeEndsAt()).isNotNull();
    }

    private User createUser(Long id) {
        User user = User.builder()
                .email("user@test.com")
                .name("tester")
                .oauthId("oauth-id")
                .oauthType(OauthType.KAKAO)
                .build();
        Profile profile = new Profile(user);
        ReflectionTestUtils.setField(profile, "profileImageType", ProfileImageType.DEFAULT);
        ReflectionTestUtils.setField(profile, "nickname", "tester");
        ReflectionTestUtils.setField(profile, "gender", Gender.U);
        ReflectionTestUtils.setField(user, "profile", profile);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post createPost(Long postId, User owner) {
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .build();
        ReflectionTestUtils.setField(destination, "id", 1L);

        Post post = Post.builder()
                .user(owner)
                .destination(destination)
                .title("제목입니다")
                .content("내용은 스무 글자 이상으로 작성합니다.")
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .recruitCapacity(4)
                .recruitDeadline(LocalDate.now().plusDays(2))
                .preferredGender(Gender.U)
                .isAgeAny(true)
                .minAge(null)
                .maxAge(null)
                .photoUrl(null)
                .tags("[]")
                .companionType(CompanionType.FULL)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
