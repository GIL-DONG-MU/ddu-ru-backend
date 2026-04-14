package com.dduru.gildongmu.journey.utils;

import com.dduru.gildongmu.journey.domain.enums.JourneyStatus;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;

import java.time.LocalDate;

public final class JourneyStatusResolver {
    private JourneyStatusResolver() {
    }

    public static JourneyStatus resolve(Post post, LocalDate today) {
        if (today.isAfter(post.getEndDate())) {
            return JourneyStatus.COMPLETED;
        }

        if (!today.isBefore(post.getStartDate())) {
            return JourneyStatus.TRAVELING;
        }

        if (post.getStatus() == PostStatus.CLOSED || post.isFull()) {
            return JourneyStatus.RECRUITMENT_CLOSED;
        }

        return JourneyStatus.RECRUITING;
    }
}
