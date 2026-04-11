package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.exception.DuplicateParticipationException;
import com.dduru.gildongmu.participation.exception.SelfParticipationNotAllowedException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationApplicantService {

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ParticipationCreateResponse participate(Long userId, Long postId, ParticipationRequest request) {
        Post post = postRepository.getActiveByIdForUpdateOrThrow(postId);
        User applicant = userRepository.getByIdOrThrow(userId);

        validateParticipationAllowed(post, applicant);

        Participation participation = Participation.createParticipation(post, applicant, request.message());
        Participation saved = saveParticipationOrThrowOnRace(participation, postId, userId);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}", saved.getId(), postId, userId);
        return new ParticipationCreateResponse(saved.getId(), saved.getStatus());
    }

    private void validateParticipationAllowed(Post post, User applicant) {
        validateNotSelfParticipation(post.getUser().getId(), applicant.getId());
        post.validateIsOpen();
        ensureNoDuplicateApplication(post.getId(), applicant.getId());
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

    private Participation saveParticipationOrThrowOnRace(Participation participation, Long postId, Long userId) {
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
