package com.dduru.gildongmu.journey.utils;

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

        if (nights <= 0) {
            return "당일 일정";
        }
        return nights + "박 " + totalDays + "일";
    }

    public static String recruitDeadlineDDay(Post post, LocalDate today) {
        LocalDate deadline = post.getRecruitDeadline();
        if (deadline == null) {
            return "상시 모집";
        }
        if (today.isAfter(deadline)) {
            return "마감";
        }

        int dDay = (int) ChronoUnit.DAYS.between(today, deadline);
        return dDay == 0 ? "D-Day" : "D-" + dDay;
    }
}
