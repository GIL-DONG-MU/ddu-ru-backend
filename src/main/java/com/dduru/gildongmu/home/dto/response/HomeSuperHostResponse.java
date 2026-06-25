package com.dduru.gildongmu.home.dto.response;

import com.dduru.gildongmu.post.domain.enums.PostStatus;

import java.time.LocalDate;
import java.util.List;

public record HomeSuperHostResponse(
        Long postId,
        PostStatus status,
        String title,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        int currentMemberCount,
        int maxMemberCount,
        List<String> tags,
        HostResponse host,
        int viewCount,
        String thumbnailUrl,
        boolean hasLiked
) {
}
