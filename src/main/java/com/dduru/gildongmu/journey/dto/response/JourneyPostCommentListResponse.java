package com.dduru.gildongmu.journey.dto.response;

import java.util.List;

public record JourneyPostCommentListResponse(
        Long journeyPostId,
        long commentCount,
        List<JourneyPostCommentResponse> comments
) {
    public static JourneyPostCommentListResponse of(
            Long journeyPostId,
            List<JourneyPostCommentResponse> comments
    ) {
        return new JourneyPostCommentListResponse(journeyPostId, comments.size(), comments);
    }
}
