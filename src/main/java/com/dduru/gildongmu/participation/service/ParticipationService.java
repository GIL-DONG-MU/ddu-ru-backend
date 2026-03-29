package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
import com.dduru.gildongmu.participation.exception.DuplicateParticipationException;
import com.dduru.gildongmu.participation.exception.ParticipationPostMismatchException;
import com.dduru.gildongmu.participation.exception.RecruitmentClosedException;
import com.dduru.gildongmu.participation.exception.SelfParticipationNotAllowedException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.dto.response.MyParticipationStatus;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ParticipationResponse participate(Long postId, Long userId, ParticipationRequest request) {
        log.debug("참여신청 시작 - postId: {}, userId: {}", postId, userId);

        Post post =  postRepository.getActiveByIdOrThrow(postId);
        User user = userRepository.getByIdOrThrow(userId);

        validateCanParticipate(post, user);

        Participation participation = Participation.createParticipation(post, user, request.message());
        Participation savedParticipation = participationRepository.save(participation);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}",
                savedParticipation.getId(), postId, userId);
        return ParticipationResponse.from(savedParticipation);
    }

    public void approveParticipation(Long postId, Long participationId, Long userId) {
        log.debug("참여신청 승인 시작 - postId: {}, participationId: {}, userId: {}", postId, participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        validateParticipationBelongsToPost(participation, postId);
        Post post = participation.getPost();
        validatePostOwner(post, userId);

        if (participation.isApproved()) {
            log.info("이미 승인된 참여신청 - participationId: {}, postId: {}", participationId, postId);
            return;
        }

        post.approveParticipation(participation);

        log.info("참여신청 승인 완료 - participationId: {}, postId: {}",
                participationId, post.getId());
    }

    public void rejectParticipation(Long postId, Long participationId, Long userId) {
        log.debug("참여신청 거절 시작 - postId: {}, participationId: {}, userId: {}", postId, participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        validateParticipationBelongsToPost(participation, postId);
        Post post = participation.getPost();
        validatePostOwner(post, userId);

        if (participation.getStatus() == ParticipationStatus.REJECTED) {
            log.info("이미 거절된 참여신청 - participationId: {}, postId: {}", participationId, postId);
            return;
        }

        post.removeApprovedParticipation(participation);
        participation.reject();

        log.info("참여신청 거절 완료 - participationId: {}", participationId);
    }

    public void cancelParticipation(Long postId, Long participationId, Long userId) {
        log.debug("참여신청 취소 시작 - postId: {}, participationId: {}, userId: {}", postId, participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        validateParticipationBelongsToPost(participation, postId);

        Post post = participation.getPost();
        validateApplicant(participation, userId);

        post.removeApprovedParticipation(participation);
        participationRepository.delete(participation);

        log.info("참여신청 삭제 완료 - participationId: {}", participationId);
    }

    @Transactional(readOnly = true)
    public List<ParticipationResponse> getParticipationsByPost(Long postId, Long userId) {
        Post post =  postRepository.getActiveByIdOrThrow(postId);

        validatePostOwner(post, userId);

        List<Participation> rows = participationRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return rows.stream().map(ParticipationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ParticipantInfo> getParticipantsForPostDetail(Post post) {
        List<ParticipantInfo> participants = new ArrayList<>();
        participants.add(ParticipantInfo.from(post.getUser(), true));
        participationRepository.findByPostIdAndStatusOrderByCreatedAtAsc(post.getId(), ParticipationStatus.APPROVED).stream()
                .map(p -> ParticipantInfo.from(p.getUser(), false))
                .forEach(participants::add);
        return participants;
    }

    @Transactional(readOnly = true)
    public MyParticipationStatus getMyParticipationStatus(Long postId, Long currentUserId, boolean isOwner) {
        if (currentUserId == null || isOwner) {
            return MyParticipationStatus.NONE;
        }

        return participationRepository.findByPostIdAndUserId(postId, currentUserId)
                .map(Participation::getStatus)
                .map(status -> switch (status) {
                    case PENDING -> MyParticipationStatus.PENDING;
                    case APPROVED -> MyParticipationStatus.APPROVED;
                    case REJECTED -> MyParticipationStatus.REJECTED;
                })
                .orElse(MyParticipationStatus.NONE);
    }

    private void validateCanParticipate(Post post, User user) {
        if (post.getUser().getId().equals(user.getId())) {
            throw new SelfParticipationNotAllowedException();
        }
        if (!post.isRecruitOpen()) {
            throw new RecruitmentClosedException();
        }
        if (participationRepository.existsByPostIdAndUserId(post.getId(), user.getId())) {
            throw new DuplicateParticipationException();
        }
    }

    private void validatePostOwner(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw PostAccessDeniedException.ownerOnly();
        }
    }

    private void validateApplicant(Participation participation, Long userId) {
        if (!participation.getUser().getId().equals(userId)) {
            throw PostAccessDeniedException.applicantOnly();
        }
    }

    private void validateParticipationBelongsToPost(Participation participation, Long postId) {
        if (!participation.getPost().getId().equals(postId)) {
            throw new ParticipationPostMismatchException(participation.getId(), postId);
        }
    }
}
