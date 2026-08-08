package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HomeSuperHostResponse(
        Long postId,
        Status status,
        String title,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        int currentMemberCount,
        int maxMemberCount,
        List<String> tags,
        HomeHostResponse host,
        int viewCount,
        String thumbnailUrl,
        boolean hasLiked
) {

    public enum Status {
        OPEN,
        CLOSED
    }
}
