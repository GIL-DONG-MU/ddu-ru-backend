package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.support.JourneyDisplayCalculator;
import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;

public record JourneyMainCardResponse(
        Long journeyId,
        Long postId,
        String title,
        String photoUrl,
        LocalDate startDate,
        LocalDate endDate,
        String tripDurationText,
        String destination,
        Integer recruitCount,
        Integer recruitCapacity,
        String recruitDeadlineDDay,
        boolean isOwner
) {
    public static JourneyMainCardResponse fromJourneyMember(JourneyMember journeyMember, LocalDate today) {
        Journey journey = journeyMember.getJourney();
        Post post = journey.getPost();
        return new JourneyMainCardResponse(
                journey.getId(),
                post.getId(),
                journey.getTitle(),
                journey.getPhotoUrl(),
                post.getStartDate(),
                post.getEndDate(),
                JourneyDisplayCalculator.tripDurationText(post),
                post.getDestination().getCity(),
                post.getRecruitCount(),
                post.getRecruitCapacity(),
                JourneyDisplayCalculator.recruitDeadlineDDay(post, today),
                journeyMember.isHost()
        );
    }
}
