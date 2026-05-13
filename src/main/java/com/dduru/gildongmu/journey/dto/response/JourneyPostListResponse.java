package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.util.List;
import java.util.Map;

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
        return of(journeyId, journeyPosts, currentUserId, hostUserId, profileImageResolver, Map.of(), Map.of());
    }

    public static JourneyPostListResponse of(
            Long journeyId,
            List<JourneyPost> journeyPosts,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver,
            Map<Long, Long> commentCountsByPostId,
            Map<Long, List<JourneyPostComment>> previewCommentsByPostId
    ) {
        List<JourneyPostResponse> posts = journeyPosts.stream()
                .map(post -> {
                    Long postId = post.getId();
                    return JourneyPostResponse.from(
                            post,
                            currentUserId,
                            hostUserId,
                            profileImageResolver,
                            commentCountsByPostId.getOrDefault(postId, 0L),
                            previewCommentsByPostId.getOrDefault(postId, List.of())
                    );
                })
                .toList();

        return new JourneyPostListResponse(journeyId, posts);
    }
}
