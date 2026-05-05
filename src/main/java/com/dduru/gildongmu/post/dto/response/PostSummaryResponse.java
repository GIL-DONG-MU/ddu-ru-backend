package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.dto.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostSummaryResponse(
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
        Gender preferredGender,
        String photoUrl,
        int viewCount,
        int likeCount,
        UserInfo author,
        boolean isSuperHost,
        LocalDateTime superHostEndsAt
) {
    public static PostSummaryResponse from(
            Post post,
            ProfileImageResolver profileImageResolver,
            LocalDate today,
            boolean isSuperHost,
            LocalDateTime superHostEndsAt
    ) {
        String photoUrl = post.getPhotoUrl();
        UserInfo authorInfo = UserInfo.from(post.getUser(), profileImageResolver);

        return new PostSummaryResponse(
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
                post.getPreferredGender(),
                photoUrl,
                post.getViewCount(),
                post.getLikeCount(),
                authorInfo,
                isSuperHost,
                superHostEndsAt
        );
    }
}
