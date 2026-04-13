package com.dduru.gildongmu.superhost.dto.response;

import com.dduru.gildongmu.post.dto.response.PostSummaryResponse;

import java.util.List;

public record SuperHostPostListResponse(
        List<PostSummaryResponse> items
) {
    public static SuperHostPostListResponse of(List<PostSummaryResponse> items) {
        return new SuperHostPostListResponse(items);
    }
}
