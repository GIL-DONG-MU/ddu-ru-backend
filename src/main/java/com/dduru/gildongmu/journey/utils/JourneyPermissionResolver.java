package com.dduru.gildongmu.journey.utils;

import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;

public final class JourneyPermissionResolver {
    private JourneyPermissionResolver() {
    }

    public static boolean isHost(Post post, Long userId) {
        return userId != null && userId.equals(post.getUser().getId());
    }

    public static boolean canEdit(Post post, Long userId, LocalDate today) {
        if (!isHost(post, userId)) {
            return false;
        }

        if (today.isAfter(post.getStartDate())) {
            return false;
        }

        LocalDate deadline = post.getRecruitDeadline();
        return deadline == null || !today.isAfter(deadline);
    }
}
