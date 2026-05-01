package com.dduru.gildongmu.journey.support;

import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class JourneyDisplayCalculator {
    private JourneyDisplayCalculator() {
    }

    public static String tripDurationText(Post post) {
        return tripDurationText(post.getStartDate(), post.getEndDate());
    }

    public static String tripDurationText(LocalDate startDate, LocalDate endDate) {
        long nightsLong = ChronoUnit.DAYS.between(startDate, endDate);
        int nights = (int) nightsLong;
        int totalDays = nights + 1;

        return nights > 0
                ? nights + "박 " + totalDays + "일"
                : "당일치기";
    }

    public static String recruitDeadlineDDay(Post post, LocalDate today) {
        LocalDate deadline = post.getRecruitDeadline();
        if (deadline == null) {
            return "상시 모집";
        }
        if (today.isAfter(deadline)) {
            return "마감";
        }

        int d = (int) ChronoUnit.DAYS.between(today, deadline);
        return d == 0 ? "D-Day" : "D-" + d;
    }
}
