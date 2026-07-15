package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record MyPagePostSummaryResponse(
        @Schema(description = "게시글 ID", example = "101")
        Long id,
        @Schema(description = "게시글 제목", example = "제주도 2박 3일 동행 구해요")
        String title,
        @Schema(description = "여행지 도시명", example = "제주")
        String destination,
        @Schema(description = "여행 시작일", example = "2026-07-10")
        LocalDate startDate,
        @Schema(description = "여행 종료일", example = "2026-07-12")
        LocalDate endDate,
        @Schema(description = "게시글 작성일", example = "2026-01-10")
        LocalDate createdAt,
        @Schema(description = "화면 표시용 게시글 상태. 여행 종료일 기준으로 계산됩니다.", example = "RECRUITING",
                allowableValues = {"RECRUITING", "RECRUITMENT_CLOSED", "TRAVEL_ENDED"})
        MyPagePostDisplayStatus displayStatus
) {
    public static MyPagePostSummaryResponse from(Post post, LocalDate today) {
        return new MyPagePostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getDestination().getCity(),
                post.getStartDate(),
                post.getEndDate(),
                post.getCreatedAt().toLocalDate(),
                MyPagePostDisplayStatus.from(post, today)
        );
    }
}
