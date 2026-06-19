package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;

import java.time.LocalDateTime;

public record PinnedNoticeInfo(
        Long journeyPostId,
        String content,
        LocalDateTime createdAt
) {
    public static PinnedNoticeInfo from(JourneyPost post) {
        return new PinnedNoticeInfo(post.getId(), post.getContent(), post.getCreatedAt());
    }
}
