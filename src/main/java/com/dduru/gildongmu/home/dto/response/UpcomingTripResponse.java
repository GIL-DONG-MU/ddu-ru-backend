package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record UpcomingTripResponse(
        Long journeyId,
        String title,
        int dDay,
        LocalDate startDate,
        LocalDate endDate,
        int currentMemberCount,
        int maxMemberCount,
        int pendingTaskCount
) {

    public static UpcomingTripResponse from(Journey journey, LocalDate today, int pendingTaskCount) {
        Post post = journey.getPost();
        int dDay = Math.max(0, (int) ChronoUnit.DAYS.between(today, post.getStartDate()));

        return new UpcomingTripResponse(
                journey.getId(),
                journey.getTitle(),
                dDay,
                post.getStartDate(),
                post.getEndDate(),
                post.getRecruitCount(),
                post.getRecruitCapacity(),
                pendingTaskCount
        );
    }
}
