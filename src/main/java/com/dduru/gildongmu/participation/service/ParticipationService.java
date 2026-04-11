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
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ParticipationCreateResponse participate(Long userId, Long postId, ParticipationRequest request) {
        Post post = postRepository.getActiveByIdForUpdateOrThrow(postId);
        User user = userRepository.getByIdOrThrow(userId);

        validateNotSelfParticipate(post.getUser().getId(), userId);
        post.validateIsOpen();
        checkDuplicateParticipation(post, user);

        Participation participation = Participation.createParticipation(post, user, request.message());
        Participation savedParticipation = saveParticipationOrThrowDuplicate(participation, postId, userId);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}",
                savedParticipation.getId(), postId, userId);
        return new ParticipationCreateResponse(savedParticipation.getId(), savedParticipation.getStatus());
    }

    private static void validateNotSelfParticipate(Long authorId, Long participantId) {
        if (authorId.equals(participantId)) {
            throw new SelfParticipationNotAllowedException();
        }
    }

    private void checkDuplicateParticipation(Post post, User user) {
        if (participationRepository.existsByPostIdAndUserId(post.getId(), user.getId())) {
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
