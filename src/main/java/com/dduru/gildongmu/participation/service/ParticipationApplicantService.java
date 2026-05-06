package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ChatRoomIds;
import com.dduru.gildongmu.participation.dto.response.MyParticipationResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.exception.DuplicateParticipationException;
import com.dduru.gildongmu.participation.exception.ParticipationApplicantAccessDeniedException;
import com.dduru.gildongmu.participation.exception.SelfParticipationNotAllowedException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationApplicantService {

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ProfileImageResolver profileImageResolver;
    private final JourneyMemberRepository journeyMemberRepository;

    public ParticipationCreateResponse participate(Long userId, Long postId, ParticipationRequest request) {
        Post post = postRepository.getActiveByIdWithLockOrThrow(postId);
        User applicant = userRepository.getByIdOrThrow(userId);

        validateParticipationAllowed(post, applicant);

        Participation participation = Participation.createParticipation(post, applicant, request.message());
        Participation saved = saveParticipationOrThrowDuplicate(participation, postId, userId);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}", saved.getId(), postId, userId);
        return new ParticipationCreateResponse(saved.getId(), saved.getStatus());
    }

    @Transactional(readOnly = true)
    public List<MyParticipationResponse> retrieveMyApplications(Long userId) {
        return participationRepository.findMyApplicationsForVisiblePosts(userId).stream()
                .map(participation -> toApplicationResponse(userId, participation))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipantInfo> getParticipantsForPostDetail(Post post) {
        return journeyMemberRepository
                .findByPostIdAndStatusWithMemberProfiles(post.getId(), JourneyMemberStatus.ACTIVE).stream()
                .map(journeyMember -> ParticipantInfo.from(journeyMember.getUser(), journeyMember.isHost(), profileImageResolver))
                .toList();
    }

    @Transactional(readOnly = true)
    public MyParticipationStatus getMyParticipationStatus(Long postId, Long currentUserId, boolean isOwner) {
        if (isOwner || currentUserId == null) {
            return MyParticipationStatus.NONE;
        }
        return participationRepository.findByPostIdAndUserId(postId, currentUserId)
                .map(ParticipationApplicantService::toMyParticipationStatus)
                .orElse(MyParticipationStatus.NONE);
    }

    public void cancelMyParticipation(Long userId, Long participationId) {
        Participation participation = participationRepository.getByIdWithLockOrThrow(participationId);
        validateApplicantOwnership(userId, participation);
        participation.validateCancellableByApplicant();

        participationRepository.delete(participation);
        log.info("참여신청 취소(삭제) - participationId: {}, postId: {}, userId: {}", participationId, participation.getPost().getId(), userId);
    }

    private static MyParticipationStatus toMyParticipationStatus(Participation participation) {
        return switch (participation.getStatus()) {
            case PENDING -> MyParticipationStatus.PENDING;
            case CONTACTING -> MyParticipationStatus.CONTACTING;
            case APPROVED -> MyParticipationStatus.APPROVED;
            case REJECTED -> MyParticipationStatus.REJECTED;
        };
    }

    private void validateParticipationAllowed(Post post, User applicant) {
        validateNotSelfParticipation(post.getUser().getId(), applicant.getId());
        post.validateIsOpen();
        ensureNoDuplicateApplication(post.getId(), applicant.getId());
    }

    private MyParticipationResponse toApplicationResponse(Long applicantUserId, Participation participation) {
        ChatRoomIds roomIds = resolveChatRoomIds(applicantUserId, participation);
        return MyParticipationResponse.from(participation, roomIds.privateRoomId(), roomIds.groupRoomId());
    }

    private ChatRoomIds resolveChatRoomIds(Long applicantUserId, Participation participation) {
        Post post = participation.getPost();
        return switch (participation.getStatus()) {
            case CONTACTING -> new ChatRoomIds(
                    findActivePrivateRoomId(applicantUserId, post),
                    null
            );
            case APPROVED -> new ChatRoomIds(
                    null,
                    findGroupRoomId(post.getId())
            );
            case PENDING, REJECTED -> new ChatRoomIds(null, null);
        };
    }

    private Long findActivePrivateRoomId(Long applicantUserId, Post post) {
        return chatRoomRepository.findPrivateRoomIdByPostAndUserIds(
                post.getId(),
                ChatRoomType.PRIVATE,
                ChatRoomStatus.ACTIVE,
                applicantUserId,
                post.getUser().getId()
        ).orElse(null);
    }

    private Long findGroupRoomId(Long postId) {
        return chatRoomRepository.findByPostIdAndRoomType(postId, ChatRoomType.GROUP)
                .map(ChatRoom::getId)
                .orElse(null);
    }

    private static void validateApplicantOwnership(Long userId, Participation participation) {
        if (!participation.getUser().getId().equals(userId)) {
            throw new ParticipationApplicantAccessDeniedException();
        }
    }

    private static void validateNotSelfParticipation(Long authorId, Long applicantId) {
        if (authorId.equals(applicantId)) {
            throw new SelfParticipationNotAllowedException();
        }
    }

    private void ensureNoDuplicateApplication(Long postId, Long applicantId) {
        if (participationRepository.existsByPostIdAndUserId(postId, applicantId)) {
            throw new DuplicateParticipationException();
        }
    }

    private Participation saveParticipationOrThrowDuplicate(Participation participation, Long postId, Long userId) {
        try {
            return participationRepository.save(participation);
        } catch (DataIntegrityViolationException e) {
            if (participationRepository.existsByPostIdAndUserId(postId, userId)) {
                log.warn("중복 참여신청 동시성 충돌 - postId: {}, userId: {}", postId, userId);
                throw new DuplicateParticipationException();
            }
            throw e;
        }
    }
}
