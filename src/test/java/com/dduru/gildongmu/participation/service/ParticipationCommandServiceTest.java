package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.service.GroupChatRoomService;
import com.dduru.gildongmu.chat.service.PrivateChatRoomService;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.response.ParticipationApproveResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipationCommandService 테스트")
class ParticipationCommandServiceTest {

    @Mock
    private PrivateChatRoomService privateChatRoomService;

    @Mock
    private GroupChatRoomService groupChatRoomService;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileImageResolver profileImageResolver;

    @InjectMocks
    private ParticipationCommandService participationCommandService;

    @Test
    @DisplayName("연락 시작 시 엔티티 상태 전이 메서드를 사용해 CONTACTING 상태를 반환한다")
    void contactParticipation_returnsContactingStatus() {
        Long participationId = 1L;
        Long ownerId = 10L;
        Long participantId = 20L;
        Long roomId = 30L;

        User owner = createUser(ownerId, "owner");
        Post post = createPost(100L, owner);
        Participation participation = createParticipation(participationId, post, participantId);

        when(participationRepository.getByIdWithLockOrThrow(participationId)).thenReturn(participation);
        when(postRepository.getActiveByIdWithLockOrThrow(post.getId())).thenReturn(post);
        when(privateChatRoomService.createOrGetRoomWithLockedPost(ownerId, post, participantId))
                .thenReturn(new PrivateChatRoomCreateResponse(roomId, true));

        ParticipationContactResponse response = participationCommandService.contactParticipation(ownerId, participationId);

        assertThat(response.privateRoomId()).isEqualTo(roomId);
        assertThat(response.status()).isEqualTo(ParticipationStatus.CONTACTING);
        assertThat(participation.isContacting()).isTrue();
        verify(privateChatRoomService).createOrGetRoomWithLockedPost(ownerId, post, participantId);
    }

    @Test
    @DisplayName("승인 시 Post.approveParticipation을 통해 APPROVED 상태를 반환한다")
    void approveParticipation_returnsApprovedStatus() {
        Long participationId = 1L;
        Long ownerId = 10L;
        Long participantId = 20L;
        Long roomId = 40L;

        User owner = createUser(ownerId, "owner");
        Post post = createPost(100L, owner);
        Participation participation = createParticipation(participationId, post, participantId);
        participation.contact();

        when(participationRepository.getByIdWithLockOrThrow(participationId)).thenReturn(participation);
        when(postRepository.getActiveByIdWithLockOrThrow(post.getId())).thenReturn(post);
        when(groupChatRoomService.inviteMemberOrGetRoom(ownerId, post.getId(), participantId))
                .thenReturn(new GroupChatInviteMemberResponse(roomId, true));

        ParticipationApproveResponse response = participationCommandService.approveParticipation(ownerId, participationId);

        assertThat(response.groupRoomId()).isEqualTo(roomId);
        assertThat(response.status()).isEqualTo(ParticipationStatus.APPROVED);
        assertThat(participation.isApproved()).isTrue();
        assertThat(post.getRecruitCount()).isEqualTo(2);
        verify(groupChatRoomService).inviteMemberOrGetRoom(ownerId, post.getId(), participantId);
    }

    private Participation createParticipation(Long participationId, Post post, Long participantId) {
        Participation participation = Participation.createParticipation(post, createUser(participantId, "participant"), "hello");
        ReflectionTestUtils.setField(participation, "id", participationId);
        return participation;
    }

    private Post createPost(Long postId, User owner) {
        Post post = Post.builder()
                .user(owner)
                .recruitCapacity(3)
                .build();
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
        return user;
    }
}
