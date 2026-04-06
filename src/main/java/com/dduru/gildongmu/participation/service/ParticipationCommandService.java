package com.dduru.gildongmu.participation.service;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostAccessDeniedException;
import com.dduru.gildongmu.post.repository.PostRepository;
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

    private final ParticipationRepository participationRepository;
    private final PostRepository postRepository;
    private final ProfileImageResolver profileImageResolver;

    /**
     * 게시글 참여자 조회 - 현재는 사용안할 예정 (조회는 아래 retrieveAllParticipants로 사용)
     */
    @Transactional(readOnly = true)
    public List<ParticipationResponse> retrieveParticipantsByPost(Long userId, Long postId) {
        Post post =  postRepository.getActiveByIdOrThrow(postId);

        validatePostOwner(post, userId);

        List<Participation> participations = participationRepository.findByPostIdOrderByCreatedAtDesc(postId);

        return participations.stream()
                .map(ParticipationResponse::from)
                .toList();
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
}
