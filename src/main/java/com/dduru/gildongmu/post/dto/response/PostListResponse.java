package com.dduru.gildongmu.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostListResponse(
        @Schema(description = "게시글 목록")
        List<PostSummaryResponse> posts,
        @Schema(description = "다음 페이지 조회용 커서 게시글 ID. hasNext=false이면 null입니다.", example = "101", nullable = true)
        Long nextCursor,
        @Schema(description = "다음 페이지 조회용 보조 커서 값. 정렬 방식에 따라 사용하며 hasNext=false이면 null입니다.", example = "12", nullable = true)
        Integer nextCursorValue,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "현재 응답에 포함된 게시글 수", example = "10")
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
