package com.dduru.gildongmu.journey.support;

import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class JourneyDisplayCalculator {
    private JourneyDisplayCalculator() {
    }

    public static String tripDurationText(Post post) {
        long nightsLong = ChronoUnit.DAYS.between(post.getStartDate(), post.getEndDate());
        int nights = (int) nightsLong;
        int totalDays = nights + 1;

        return nights > 0
                ? nights + "박 " + totalDays + "일"
                : "당일치기";
    }

    public static String startDDay(Post post, LocalDate today) {
        LocalDate startDate = post.getStartDate();
        LocalDate endDate = post.getEndDate();

        if (today.isAfter(endDate)) return "완료";
        if (today.isAfter(startDate)) return "여행 중";
        if (today.isEqual(startDate)) return "D-Day";

        int d = (int) ChronoUnit.DAYS.between(today, startDate);
        return "D-" + d;
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
