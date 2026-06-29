package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.journey.support.JourneyDisplayCalculator;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        @Schema(description = "게시글 ID", example = "101")
        Long id,
        @Schema(description = "게시글 제목", example = "제주도 2박 3일 동행 구해요")
        String title,
        @Schema(description = "게시글 본문", example = "제주도 서쪽 위주로 천천히 여행하실 분을 찾습니다.")
        String content,
        @Schema(description = "게시글 모집 상태", example = "OPEN", allowableValues = {"OPEN", "CLOSED"})
        PostStatus status,
        @Schema(description = "모집 정원이 가득 찼는지 여부", example = "false")
        boolean isFull,
        @Schema(description = "모집 마감일까지 남은 일수. 마감일이 지났으면 음수일 수 있습니다.", example = "3")
        int daysUntilRecruitDeadline,
        @Schema(description = "여행 시작일까지 남은 일수. 시작일이 지났으면 음수일 수 있습니다.", example = "7")
        int daysUntilTravelStart,
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
        @Schema(description = "모집 마감일", example = "2026-07-09")
        LocalDate recruitDeadline,
        @Schema(description = "선호 성별. U는 성별 무관입니다.", example = "U", allowableValues = {"M", "F", "U"})
        Gender preferredGender,
        @Schema(description = "선호 연령 무관 여부", example = "false")
        boolean isAgeAny,
        @Schema(description = "선호 최소 나이. isAgeAny=true이면 null입니다.", example = "20", nullable = true)
        Integer minAge,
        @Schema(description = "선호 최대 나이. isAgeAny=true이면 null입니다.", example = "29", nullable = true)
        Integer maxAge,
        @Schema(description = "게시글 대표 이미지 URL. 없으면 null입니다.", example = "https://cdn.example.com/posts/cover.jpg", nullable = true)
        String photoUrl,
        @Schema(description = "게시글 태그 목록", example = "[\"맛집\", \"힐링\"]")
        List<String> tags,
        @Schema(description = "조회 수", example = "128")
        int viewCount,
        @Schema(description = "좋아요 수", example = "12")
        int likeCount,
        @Schema(description = "게시글 생성 시각", example = "2026-06-29T12:34:56")
        LocalDateTime createdAt,
        @Schema(description = "작성자 정보")
        PostAuthorInfo author,
        @Schema(description = "현재 사용자가 게시글 작성자인지 여부", example = "false")
        boolean isOwner,
        @Schema(description = "현재 사용자가 게시글을 수정할 수 있는지 여부", example = "false")
        boolean canEditPost,
        @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
        boolean hasLiked,
        @Schema(description = "참여 확정 멤버 목록. 호스트가 포함됩니다.")
        List<ParticipantInfo> participants,
        @Schema(description = "현재 사용자의 이 게시글 참여 상태", example = "PENDING", nullable = true)
        MyParticipationStatus myParticipationStatus,
        @Schema(description = "여행 기간 표시 문구", example = "2박 3일")
        String tripDurationText,
        @Schema(description = "모집 마감 D-Day 표시 문구", example = "D-3")
        String recruitDeadlineDDay,
        @Schema(description = "동행 방식", example = "FULL", allowableValues = {"FULL", "PARTIAL", "MEAL"})
        CompanionType companionType
) {
    public static PostDetailResponse from(Post post, JsonConverter jsonConverter,
                                          LocalDate today,
                                          boolean isOwner,
                                          boolean canEditPost,
                                          boolean hasLiked,
                                          List<ParticipantInfo> participants,
                                          MyParticipationStatus myParticipationStatus,
                                          ProfileImageResolver profileImageResolver,
                                          boolean isAuthorSuperHost) {
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());
        PostAuthorInfo authorInfo = PostAuthorInfo.from(post.getUser(), isAuthorSuperHost, profileImageResolver, today);

        String tripDurationText = JourneyDisplayCalculator.tripDurationText(post);
        String recruitDeadlineDDay = JourneyDisplayCalculator.recruitDeadlineDDay(post, today);

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.isFull(),
                post.getDaysUntilRecruitDeadline(today),
                post.getDaysUntilTravelStart(today),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getRecruitDeadline(),
                post.getPreferredGender(),
                post.isAgeAny(),
                post.getMinAge(),
                post.getMaxAge(),
                post.getPhotoUrl(),
                tags,
                post.getViewCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                authorInfo,
                isOwner,
                canEditPost,
                hasLiked,
                participants,
                myParticipationStatus,
                tripDurationText,
                recruitDeadlineDDay,
                post.getCompanionType()
        );
    }
}
