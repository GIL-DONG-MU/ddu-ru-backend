package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.service.GroupChatRoomService;
import com.dduru.gildongmu.chat.service.PrivateChatRoomService;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationApproveResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.dduru.gildongmu.participation.exception.InvalidParticipationStatusException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationCommandService {

    private final PrivateChatRoomService privateChatRoomService;
    private final GroupChatRoomService groupChatRoomService;
    private final ParticipationRepository participationRepository;
    private final ProfileImageResolver profileImageResolver;

    public ParticipationContactResponse contactParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        validatePostOwner(post, userId);
        post.validateIsOpen();
        validateParticipationStatus(participation);

        PrivateChatRoomCreateResponse room = privateChatRoomService.createOrGetRoom(userId, post, participation.getUser().getId());

        updateStatusToContactingIfNew(participation);

        loggingStatusChange(participation);

        return new ParticipationContactResponse(
                participation.getId(),
                room.roomId(),
                room.isCreated(),
                participation.getStatus()
        );
    }

    public ParticipationApproveResponse approveParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();
        Long inviteeUserId = participation.getUser().getId();

        validatePostOwner(post, userId);
        post.validateIsOpen();
        validateParticipationStatus(participation);

        GroupChatInviteMemberResponse response = groupChatRoomService.inviteMemberOrGetRoom(userId, post.getId(), participation.getUser().getId());

        post.approveParticipation(participation);
        loggingStatusChange(participation);

        return new ParticipationApproveResponse(
                participation.getId(),
                inviteeUserId,
                response.roomId(),
                participation.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<ParticipationRetrieveResponse> retrieveAllParticipants(Long userId, ParticipationRetrieveRequest request) {
        return participationRepository.findReceivedRequestsByStatus(userId, request.status()).stream()
                .map(queryResult -> ParticipationRetrieveResponse.from(queryResult, profileImageResolver))
                .toList();
    }

    private static void updateStatusToContactingIfNew(Participation participation) {
        if (participation.isPending()) {
            participation.contact();
        }
    }

    private static void validatePostOwner(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
    }

    private static void validateParticipationStatus(Participation participation) {
        if (participation.isRejected() || participation.isApproved()) {
            throw new InvalidParticipationStatusException();
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
