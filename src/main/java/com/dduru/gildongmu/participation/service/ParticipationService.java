package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.exception.DuplicateParticipationException;
import com.dduru.gildongmu.participation.exception.RecruitmentClosedException;
import com.dduru.gildongmu.participation.exception.SelfParticipationNotAllowedException;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Post post =  postRepository.getActiveByIdOrThrow(postId);
        User user = userRepository.getByIdOrThrow(userId);

        validateCanParticipate(post, user);

        Participation participation = Participation.createParticipation(post, user, request.message());
        participationRepository.save(participation);

        log.info("참여신청 완료 - participationId: {}, postId: {}, userId: {}",
                participation.getId(), postId, userId);
        return new ParticipationCreateResponse(participation.getId(), participation.getStatus());
    }

    private void validateCanParticipate(Post post, User user) {
        if (post.getUser().getId().equals(user.getId())) {
            throw new SelfParticipationNotAllowedException();
        }
        if (post.isClosed()) {
            throw new RecruitmentClosedException();
        }
        if (participationRepository.existsByPostIdAndUserId(post.getId(), user.getId())) {
            throw new DuplicateParticipationException();
        }
    }
}
