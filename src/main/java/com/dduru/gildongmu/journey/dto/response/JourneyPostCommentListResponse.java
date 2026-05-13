package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.util.List;

public record JourneyPostCommentListResponse(
        Long journeyPostId,
        long commentCount,
        boolean hasMore,
        List<JourneyPostCommentResponse> comments
) {
    public static JourneyPostCommentListResponse of(
            Long journeyPostId,
            long commentCount,
            boolean hasMore,
            List<JourneyPostComment> comments,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        return new JourneyPostCommentListResponse(
                journeyPostId,
                commentCount,
                hasMore,
                comments.stream()
                        .map(comment -> JourneyPostCommentResponse.from(
                                comment,
                                currentUserId,
                                hostUserId,
                                profileImageResolver
                        ))
                        .toList()
        );
    }
}
