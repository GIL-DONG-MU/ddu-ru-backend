package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
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

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;


    @Transactional(readOnly = true)
    public List<ParticipationResponse> retrieveParticipantsByPost(Long userId, Long postId) {
        Post post =  postRepository.getActiveByIdOrThrow(postId);

        validatePostOwner(post, userId);

        List<Participation> applicants = participationRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return applicants.stream()
                .map(ParticipationResponse::from)
                .toList();
    }

    private static void validatePostOwner(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
    }
}
