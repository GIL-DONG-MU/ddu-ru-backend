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
        validateCapacity(post);

        participation.approve();
        post.incrementRecruitCount();

        log.info("참여신청 승인 완료 - participationId: {}, postId: {}",
                participationId, post.getId());
    }

    public void rejectParticipation(Long participationId, Long userId) {
        log.debug("참여신청 거절 시작 - participationId: {}, userId: {}", participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        validatePostOwner(post, userId);

        boolean wasApproved = participation.isApproved();
        participation.reject();
        if (wasApproved) {
            post.decrementRecruitCount();
            log.info("승인된 신청 거절로 모집인원 감소 - postId: {}, recruitCount: {}", post.getId(), post.getRecruitCount());
        }

        log.info("참여신청 거절 완료 - participationId: {}, wasApproved: {}", participationId, wasApproved);
    }

    public void cancelParticipation(Long participationId, Long userId) {
        log.debug("참여신청 취소 시작 - participationId: {}, userId: {}", participationId, userId);

        Participation participation = participationRepository.getByIdOrThrow(participationId);
        Post post = participation.getPost();

        if (!participation.getUser().getId().equals(userId)) {
            throw new PostAccessDeniedException();
        }

        boolean wasApproved = participation.isApproved();
        participationRepository.delete(participation);
        if (wasApproved) {
            post.decrementRecruitCount();
            log.info("승인된 신청 삭제로 모집인원 감소 - postId: {}, recruitCount: {}", post.getId(), post.getRecruitCount());
        }

        log.info("참여신청 삭제 완료 - participationId: {}, wasApproved: {}", participationId, wasApproved);
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
            throw new PostAccessDeniedException();
        }
    }

    private void validateCapacity(Post post) {
        int approved = participationRepository.countApprovedParticipationsByPostId(post.getId());

        if (post.getRecruitCount() != approved) {
            log.warn("모집인원 불일치 감지 - postId: {}, Post.recruitCount: {}, 실제 승인수: {}", post.getId(), post.getRecruitCount(), approved);
        }
        if (approved >= post.getRecruitCapacity()) {
            throw new RecruitmentClosedException();
        }
    }
}
