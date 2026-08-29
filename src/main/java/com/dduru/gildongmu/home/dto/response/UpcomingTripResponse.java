package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

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
        @Schema(description = "여행 일정 개수", example = "3")
        int scheduleCount
) {

    public static UpcomingTripResponse from(Journey journey, LocalDate today, int scheduleCount) {
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
                scheduleCount
        );
    }
}
