package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.ParticipationResponse;
import com.dduru.gildongmu.participation.exception.DuplicateParticipationException;
import com.dduru.gildongmu.participation.exception.RecruitmentClosedException;
import com.dduru.gildongmu.participation.exception.SelfParticipationNotAllowedException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        validateParticipation(post, user);

        Participation participation = Participation.createParticipation(post, user, request.message());
        Participation savedParticipation = participationRepository.save(participation);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}",
                savedParticipation.getId(), postId, userId);
        return ParticipationResponse.from(savedParticipation);
    }

    public void approveParticipation(Long participationId, Long userId) {
        log.debug("참여신청 승인 시작 - participationId: {}, userId: {}", participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        validatePostOwner(post, userId);

        post.approveParticipation(participation);

        log.info("참여신청 승인 완료 - participationId: {}, postId: {}",
                participationId, post.getId());
    }

    public void rejectParticipation(Long participationId, Long userId) {
        log.debug("참여신청 거절 시작 - participationId: {}, userId: {}", participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        validatePostOwner(post, userId);

        post.removeApprovedParticipation(participation);
        participation.reject();

        log.info("참여신청 거절 완료 - participationId: {}", participationId);
    }

    public void cancelParticipation(Long participationId, Long userId) {
        log.debug("참여신청 취소 시작 - participationId: {}, userId: {}", participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        validateParticipationAccess(participation, userId);

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

    private void validateParticipation(Post post, User user) {
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

    private void validateParticipationAccess(Participation participation, Long userId) {
        if (!participation.getUser().getId().equals(userId)) {
            throw PostAccessDeniedException.applicantOnly();
        }
    }
}
