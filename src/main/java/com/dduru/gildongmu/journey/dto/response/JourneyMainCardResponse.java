package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.support.JourneyDisplayCalculator;
import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;

public record JourneyMainCardResponse(
        Long postId,
        String title,
        String photoUrl,
        LocalDate startDate,
        LocalDate endDate,
        String tripDurationText,
        String destination,
        Integer recruitCount,
        Integer recruitCapacity,
        boolean isOwner
) {
    public static JourneyMainCardResponse fromJourneyMember(JourneyMember journeyMember) {
        Post post = journeyMember.getPost();
        return new JourneyMainCardResponse(
                post.getId(),
                post.getTitle(),
                post.getPhotoUrl(),
                post.getStartDate(),
                post.getEndDate(),
                JourneyDisplayCalculator.tripDurationText(post),
                post.getDestination().getCity(),
                post.getRecruitCount(),
                post.getRecruitCapacity(),
                journeyMember.isHost()
        );
    }
}
