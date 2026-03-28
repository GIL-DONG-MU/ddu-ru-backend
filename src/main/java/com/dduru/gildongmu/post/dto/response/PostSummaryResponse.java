package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.dto.UserInfo;

import java.time.LocalDate;

public record PostSummaryResponse(
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
        Gender preferredGender,
        String photoUrl,
        int viewCount,
        int likeCount,
        UserInfo author
) {
    public static PostSummaryResponse from(Post post, ProfileImageResolver profileImageResolver) {
        String photoUrl = post.getPhotoUrl();
        UserInfo authorInfo = UserInfo.from(post.getUser(), profileImageResolver);

        return new PostSummaryResponse(
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
                post.getPreferredGender(),
                photoUrl,
                post.getViewCount(),
                post.getLikeCount(),
                authorInfo
        );
    }
}
