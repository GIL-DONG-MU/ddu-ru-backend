package com.dduru.gildongmu.journey.dto.response;

import java.util.List;

public record JourneyPostListResponse(
        Long journeyId,
        List<JourneyPostResponse> posts,
        Long nextCursor,
        boolean hasNext,
        int size
) {
    public static JourneyPostListResponse of(
            Long journeyId,
            List<JourneyPostResponse> posts,
            boolean hasNext
    ) {
        Long nextCursor = hasNext && !posts.isEmpty()
                ? posts.get(posts.size() - 1).journeyPostId()
                : null;

        return new JourneyPostListResponse(journeyId, posts, nextCursor, hasNext, posts.size());
    }
}
