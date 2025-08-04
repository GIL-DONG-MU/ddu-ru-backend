package com.dduru.gildongmu.post.dto;

import java.util.List;

public record PostListResponse(
        List<PostSummaryDto> posts,
        Long nextCursor,
        boolean hasNext,
        int size
) {
    public static PostListResponse of(List<PostSummaryDto> posts, boolean hasNext) {
        Long nextCursor = null;
        if (hasNext && !posts.isEmpty()) {
            nextCursor = posts.get(posts.size() - 1).id();
        }

        return new PostListResponse(
                posts,
                nextCursor,
                hasNext,
                posts.size()
        );
    }
}
