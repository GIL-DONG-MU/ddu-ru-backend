package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.util.List;

public record JourneyPostListResponse(
        Long journeyId,
        List<JourneyPostResponse> posts
) {
    public static JourneyPostListResponse of(
            Long journeyId,
            List<JourneyPost> journeyPosts,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        List<JourneyPostResponse> posts = journeyPosts.stream()
                .map(post -> JourneyPostResponse.from(post, currentUserId, hostUserId, profileImageResolver))
                .toList();

        return new JourneyPostListResponse(journeyId, posts);
    }
}
