package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

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
            List<JourneyPost> journeyPosts,
            boolean hasNext,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        List<JourneyPostResponse> posts = journeyPosts.stream()
                .map(post -> JourneyPostResponse.from(post, currentUserId, hostUserId, profileImageResolver))
                .toList();
        Long nextCursor = hasNext && !journeyPosts.isEmpty()
                ? journeyPosts.get(journeyPosts.size() - 1).getId()
                : null;

        return new JourneyPostListResponse(
                journeyId,
                posts,
                nextCursor,
                hasNext,
                posts.size()
        );
    }
}
