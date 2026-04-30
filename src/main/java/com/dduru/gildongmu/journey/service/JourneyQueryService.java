package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.journey.dto.response.JourneyMainCardResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JourneyQueryService {

    private final PostRepository postRepository;
    private final ParticipationRepository participationRepository;

    public JourneyMainListResponse retrieveMyJourneys(Long userId) {
        LocalDate today = LocalDate.now();

        List<Post> activeOwnedPosts = postRepository.findActiveJourneyPostsByOwnerId(userId, today);
        List<Participation> activeApprovedParticipations = participationRepository
                .findActiveJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, today);
        List<Post> completedOwnedPosts = postRepository.findCompletedJourneyPostsByOwnerId(userId, today);
        List<Participation> completedApprovedParticipations = participationRepository
                .findCompletedJourneyParticipationsByUserIdAndStatus(userId, ParticipationStatus.APPROVED, today);

        Comparator<JourneyMainCardResponse> activeSortOrder = Comparator
                .comparing(JourneyMainCardResponse::startDate)
                .thenComparing(JourneyMainCardResponse::postId, Comparator.reverseOrder());
        Comparator<JourneyMainCardResponse> completedSortOrder = Comparator
                .comparing(JourneyMainCardResponse::endDate, Comparator.reverseOrder())
                .thenComparing(JourneyMainCardResponse::postId, Comparator.reverseOrder());

        List<JourneyMainCardResponse> activeJourneys = mergeJourneyCards(
                activeOwnedPosts,
                activeApprovedParticipations,
                activeSortOrder
        );
        List<JourneyMainCardResponse> completedJourneys = mergeJourneyCards(
                completedOwnedPosts,
                completedApprovedParticipations,
                completedSortOrder
        );

        return JourneyMainListResponse.of(activeJourneys, completedJourneys);
    }

    private List<JourneyMainCardResponse> mergeJourneyCards(
            List<Post> ownedPosts,
            List<Participation> approvedParticipations,
            Comparator<JourneyMainCardResponse> sortOrder
    ) {
        Map<Long, JourneyMainCardResponse> cardsByPostId = Stream.concat(
                        ownedPosts.stream().map(JourneyMainCardResponse::fromHost),
                        approvedParticipations.stream().map(JourneyMainCardResponse::fromParticipation)
                )
                .sorted(sortOrder)
                .collect(LinkedHashMap::new,
                        (map, item) -> map.putIfAbsent(item.postId(), item),
                        LinkedHashMap::putAll);

        return List.copyOf(cardsByPostId.values());
    }
}
