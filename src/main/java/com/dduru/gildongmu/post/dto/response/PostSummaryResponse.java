package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDate;

public record PostSummaryResponse(
        Long id,
        String title,
        PostStatus status,
        boolean isFull,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        Integer recruitCapacity,
        Integer recruitCount,
        Gender preferredGender,
        CompanionType companionType,
        String photoUrl,
        int likeCount,
        boolean hasLiked,
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
