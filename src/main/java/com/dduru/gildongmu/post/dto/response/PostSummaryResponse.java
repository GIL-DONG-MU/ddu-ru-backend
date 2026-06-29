package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record PostSummaryResponse(
        @Schema(description = "게시글 ID", example = "101")
        Long id,
        @Schema(description = "게시글 제목", example = "제주도 2박 3일 동행 구해요")
        String title,
        @Schema(description = "게시글 모집 상태", example = "OPEN", allowableValues = {"OPEN", "CLOSED"})
        PostStatus status,
        @Schema(description = "모집 정원이 가득 찼는지 여부", example = "false")
        boolean isFull,
        @Schema(description = "여행 시작일", example = "2026-07-10")
        LocalDate startDate,
        @Schema(description = "여행 종료일", example = "2026-07-12")
        LocalDate endDate,
        @Schema(description = "여행지 도시명", example = "제주")
        String destination,
        @Schema(description = "모집 정원. 호스트를 포함한 총 인원입니다.", example = "4")
        Integer recruitCapacity,
        @Schema(description = "현재 참여 확정 인원 수", example = "2")
        Integer recruitCount,
        @Schema(description = "선호 성별. U는 성별 무관입니다.", example = "U", allowableValues = {"M", "F", "U"})
        Gender preferredGender,
        @Schema(description = "동행 방식", example = "FULL", allowableValues = {"FULL", "PARTIAL", "MEAL"})
        CompanionType companionType,
        @Schema(description = "게시글 대표 이미지 URL. 없으면 null입니다.", example = "https://cdn.example.com/posts/cover.jpg", nullable = true)
        String photoUrl,
        @Schema(description = "좋아요 수", example = "12")
        int likeCount,
        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        boolean hasLiked,
        @Schema(description = "작성자 정보")
        PostAuthorInfo author
) {
    public static PostSummaryResponse from(
            Post post,
            ProfileImageResolver profileImageResolver,
            LocalDate today,
            boolean isSuperHost,
            boolean hasLiked
    ) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getStatus(),
                post.isFull(),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getPreferredGender(),
                post.getCompanionType(),
                post.getPhotoUrl(),
                post.getLikeCount(),
                hasLiked,
                PostAuthorInfo.from(post.getUser(), isSuperHost, profileImageResolver, today)
        );
    }
}
