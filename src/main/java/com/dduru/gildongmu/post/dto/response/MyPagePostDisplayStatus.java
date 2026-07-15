package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;

import java.time.LocalDate;

public enum MyPagePostDisplayStatus {
    RECRUITING,
    RECRUITMENT_CLOSED,
    TRAVEL_ENDED;

    public static MyPagePostDisplayStatus from(Post post, LocalDate today) {
        if (post.hasTravelEnded(today)) {
            return TRAVEL_ENDED;
        }
        if (post.getStatus() == PostStatus.OPEN && !post.isFull()) {
            return RECRUITING;
        }
        return RECRUITMENT_CLOSED;
    }
}
