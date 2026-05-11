package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipationApplicantService 테스트")
class ParticipationApplicantServiceTest {

    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ProfileImageResolver profileImageResolver;
    @Mock
    private JourneyMemberRepository journeyMemberRepository;

    @InjectMocks
    private ParticipationApplicantService participationApplicantService;

    @Nested
    @DisplayName("내 참여 상태 조회")
    class GetMyParticipationStatus {

        @Test
        @DisplayName("승인 이력이 있어도 여정 멤버가 REMOVED이면 REMOVED_BY_HOST를 반환한다")
        void approvedParticipationWithRemovedJourneyMemberReturnsRemovedByHost() {
            Long postId = 1L;
            Long userId = 20L;
            Participation participation = createApprovedParticipation(postId, userId);

            when(participationRepository.findByPostIdAndUserId(postId, userId))
                    .thenReturn(Optional.of(participation));
            when(journeyMemberRepository.findStatusByJourneyPostIdAndUserId(postId, userId))
                    .thenReturn(Optional.of(JourneyMemberStatus.REMOVED));

            MyParticipationStatus status = participationApplicantService.getMyParticipationStatus(postId, userId, false);

            assertThat(status).isEqualTo(MyParticipationStatus.REMOVED_BY_HOST);
        }
    }

    private Participation createApprovedParticipation(Long postId, Long userId) {
        User author = createUser(10L, "author");
        User participant = createUser(userId, "participant");
        Post post = Post.createPost(
                author,
                null,
                "참여 상태 테스트 게시글",
                "참여 상태 테스트 본문은 충분히 긴 내용입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                3,
                LocalDate.now().plusDays(1),
                null,
                true,
                null,
                null,
                null,
                "[]",
                null
        );
        ReflectionTestUtils.setField(post, "id", postId);

        Participation participation = Participation.createParticipation(post, participant, "hello");
        participation.approve();
        return participation;
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
}
