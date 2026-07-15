package com.dduru.gildongmu.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MyPageLikedPostListResponse(
        @Schema(description = "찜한 게시글 목록")
        List<MyPageLikedPostSummaryResponse> posts,
        @Schema(description = "다음 페이지 조회용 커서 (PostLike ID). hasNext=false이면 null입니다.", nullable = true)
        Long nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "현재 응답에 포함된 게시글 수", example = "10")
        int size
) {
    public static MyPageLikedPostListResponse of(
            List<MyPageLikedPostSummaryResponse> posts,
            boolean hasNext,
            Long lastLikeId
    ) {
        Long nextCursor = hasNext ? lastLikeId : null;
        return new MyPageLikedPostListResponse(posts, nextCursor, hasNext, posts.size());
    }
}
