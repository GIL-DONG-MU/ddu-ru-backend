package com.dduru.gildongmu.post.dto.response;

import java.util.List;

public record PostListResponse(
        List<PostSummaryResponse> posts,
        Long nextCursor,
        Integer nextCursorValue,
        boolean hasNext,
        int size
) {
    public static PostListResponse of(List<PostSummaryResponse> posts, boolean hasNext, Integer nextCursorValue) {
        Long nextCursor = null;
        if (hasNext && !posts.isEmpty()) {
            nextCursor = posts.get(posts.size() - 1).id();
        }

        return new PostListResponse(
                posts,
                nextCursor,
                nextCursorValue,
                hasNext,
                posts.size()
        );
    }
}
