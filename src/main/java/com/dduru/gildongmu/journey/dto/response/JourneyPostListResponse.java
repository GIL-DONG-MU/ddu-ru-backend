package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.util.List;
import java.util.Map;

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
            ProfileImageResolver profileImageResolver,
            Map<Long, Long> commentCountByPostId
    ) {
        List<JourneyPostResponse> posts = journeyPosts.stream()
                .map(post -> JourneyPostResponse.from(
                        post, currentUserId, hostUserId, profileImageResolver,
                        commentCountByPostId.getOrDefault(post.getId(), 0L)
                ))
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
