package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.util.List;

public record JourneyPostCommentListResponse(
        Long journeyPostId,
        long commentCount,
        List<JourneyPostCommentResponse> comments
) {
    public static JourneyPostCommentListResponse of(
            Long journeyPostId,
            List<JourneyPostComment> comments,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        List<JourneyPostCommentResponse> commentResponses = comments.stream()
                .map(comment -> JourneyPostCommentResponse.from(
                        comment,
                        journeyPostId,
                        currentUserId,
                        hostUserId,
                        profileImageResolver
                ))
                .toList();
        return new JourneyPostCommentListResponse(journeyPostId, commentResponses.size(), commentResponses);
    }
}
