package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.service.GroupChatRoomService;
import com.dduru.gildongmu.chat.service.PrivateChatRoomService;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationApproveResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 참여 신청 커맨드(연락·승인·거절).
 * <p>
 * <b>락 순서:</b> 동일 트랜잭션에서 {@code Participation} 행을 {@code FOR UPDATE}로 잠근 뒤
 * {@code Post} 행을 같은 방식으로 잠근다. 다른 코드 경로에서 {@code Post}를 먼저 잠그고
 * {@code Participation}을 잡지 않도록 유지해야 데드락 위험을 줄일 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationCommandService {

    private final PrivateChatRoomService privateChatRoomService;
    private final GroupChatRoomService groupChatRoomService;
    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final ProfileImageResolver profileImageResolver;
    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;

    public ParticipationContactResponse contactParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdWithLockOrThrow(participationId);
        Post lockedPost = postRepository.getActiveByIdWithLockOrThrow(participation.getPost().getId());
        Long participantUserId = participation.getUser().getId();

        validatePostOwner(lockedPost, userId);
        lockedPost.validateIsOpen();

        PrivateChatRoomCreateResponse room = privateChatRoomService.createOrGetRoomWithLockedPost(userId, lockedPost, participantUserId);

        participation.contact();
        loggingStatusChange(participation);

        return new ParticipationContactResponse(
                participation.getId(),
                participantUserId,
                room.roomId(),
                participation.getStatus()
        );
    }

    public ParticipationApproveResponse approveParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdWithLockOrThrow(participationId);
        Post lockedPost = postRepository.getActiveByIdWithLockOrThrow(participation.getPost().getId());
        Long participantUserId = participation.getUser().getId();

        validatePostOwner(lockedPost, userId);
        lockedPost.validateIsOpen();

        lockedPost.approveParticipation(participation);
        Journey journey = journeyRepository.getByPostIdOrThrow(lockedPost.getId());
        // 승인 이후 실제 협업 멤버십을 만든다. 신청 이력은 participations에, 협업 멤버는 journey_members에 남긴다.
        journeyMemberRepository.findByJourneyIdAndUserId(journey.getId(), participantUserId)
                .ifPresentOrElse(
                        JourneyMember::activate,
                        () -> journeyMemberRepository.save(JourneyMember.createMember(journey, participation.getUser()))
                );
        GroupChatInviteMemberResponse response = groupChatRoomService.inviteMemberOrGetRoom(userId, journey.getId(), participantUserId);
        loggingStatusChange(participation);

        return new ParticipationApproveResponse(
                participation.getId(),
                participantUserId,
                response.roomId(),
                participation.getStatus()
        );
    }

    public void rejectParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdWithLockOrThrow(participationId);
        Post lockedPost = postRepository.getActiveByIdWithLockOrThrow(participation.getPost().getId());

        validatePostOwner(lockedPost, userId);
        lockedPost.validateIsOpen();

        participation.reject();
        loggingStatusChange(participation);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRetrieveResponse> retrieveAllParticipants(Long userId, ParticipationRetrieveRequest request) {
        return participationRepository.findReceivedRequestsByStatus(userId, request.status()).stream()
                .map(queryResult -> ParticipationRetrieveResponse.from(queryResult, profileImageResolver))
                .toList();
    }

    private static void validatePostOwner(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
    }

    /**
     * 참여신청 상태 변경 로그 - 상태 변경이 일어나는 모든 곳에서 호출
     * @param participation
     */
    private static void loggingStatusChange(Participation participation) {
        log.info("참여신청 상태 변경 - participationId: {}, postId: {}, userId: {}, newStatus: {}",
                participation.getId(), participation.getPost().getId(),
                participation.getUser().getId(), participation.getStatus());
    }
}
