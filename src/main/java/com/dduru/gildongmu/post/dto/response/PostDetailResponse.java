package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.journey.support.JourneyDisplayCalculator;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        PostStatus status,
        boolean isFull,
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
        String photoUrl,
        List<String> tags,
        int viewCount,
        int likeCount,
        LocalDateTime createdAt,
        PostAuthorInfo author,
        boolean isOwner,
        boolean canEditPost,
        boolean hasLiked,
        List<ParticipantInfo> participants,
        MyParticipationStatus myParticipationStatus,
        String tripDurationText,
        String recruitDeadlineDDay,
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
        PostAuthorInfo authorInfo = PostAuthorInfo.from(post.getUser(), isAuthorSuperHost, profileImageResolver);

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
