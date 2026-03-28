package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.dto.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        boolean isRecruitOpen,
        int daysUntilRecruitDeadline,
        int daysUntilTravelStart,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        Integer recruitCapacity,
        Integer recruitCount,
        LocalDate recruitDeadline,
        Gender preferredGender,
        boolean isAgeAny,
        Integer minAge,
        Integer maxAge,
        List<String> photoUrls,
        List<String> tags,
        int viewCount,
        int likeCount,
        LocalDateTime createdAt,
        UserInfo author,
        boolean isOwner,
        boolean hasLiked,
        List<ParticipantInfo> participants,
        MyParticipationStatus myParticipationStatus,
        String tripDurationText,
        String recruitDeadlineDDay
) {
    public static PostDetailResponse from(Post post, JsonConverter jsonConverter,
                                          boolean isOwner,
                                          boolean hasLiked,
                                          List<ParticipantInfo> participants,
                                          MyParticipationStatus myParticipationStatus,
                                          ProfileImageResolver profileImageResolver) {
        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());
        UserInfo authorInfo = UserInfo.from(post.getUser(), profileImageResolver);

        long nightsLong = ChronoUnit.DAYS.between(post.getStartDate(), post.getEndDate());
        int nights = (int) nightsLong;
        int totalDays = nights + 1;
        String tripDurationText = nights > 0
                ? nights + "박 " + totalDays + "일"
                : "당일 일정";

        String recruitDeadlineDDay;
        LocalDate deadline = post.getRecruitDeadline();
        if (deadline == null) {
            recruitDeadlineDDay = "상시 모집";
        } else if (LocalDate.now().isAfter(deadline)) {
            recruitDeadlineDDay = "마감";
        } else {
            int d = post.getDaysUntilRecruitDeadline();
            recruitDeadlineDDay = (d == 0) ? "D-Day" : "D-" + d;
        }

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.isRecruitOpen(),
                post.getDaysUntilRecruitDeadline(),
                post.getDaysUntilTravelStart(),
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
                photoUrls,
                tags,
                post.getViewCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                authorInfo,
                isOwner,
                hasLiked,
                participants,
                myParticipationStatus,
                tripDurationText,
                recruitDeadlineDDay
        );
    }
}
