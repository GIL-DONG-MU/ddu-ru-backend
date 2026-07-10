package com.dduru.gildongmu.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MyPagePostListResponse(
        @Schema(description = "게시글 목록")
        List<MyPagePostSummaryResponse> posts,
        @Schema(description = "다음 페이지 조회용 커서 게시글 ID. hasNext=false이면 null입니다.", example = "101", nullable = true)
        Long nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "현재 응답에 포함된 게시글 수", example = "10")
        int size,
        @Schema(description = "모집 중인 내 게시글 수 (status=OPEN, 정원 미달, 여행 미종료)", example = "2")
        int currentRecruitingCount,
        @Schema(description = "모집 중 + 모집 완료 게시글 수 (여행 종료 제외)", example = "5")
        int activePostCount
) {
    public static MyPagePostListResponse of(
            List<MyPagePostSummaryResponse> posts,
            boolean hasNext,
            int currentRecruitingCount,
            int activePostCount
    ) {
        Long nextCursor = hasNext && !posts.isEmpty() ? posts.get(posts.size() - 1).id() : null;
        return new MyPagePostListResponse(posts, nextCursor, hasNext, posts.size(), currentRecruitingCount, activePostCount);
    }
}
