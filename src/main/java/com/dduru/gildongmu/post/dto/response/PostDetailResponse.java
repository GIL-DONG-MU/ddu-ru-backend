package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.dto.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        List<AgeRange> preferredAges,
        List<String> photoUrls,
        List<String> tags,
        int viewCount,
        int likeCount,
        LocalDateTime createdAt,
        UserInfo author,
        boolean isOwner,
        boolean hasApplied
) {
    public static PostDetailResponse from(Post post, JsonConverter jsonConverter, boolean isOwner, boolean hasApplied) {
        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());
        List<AgeRange> preferredAges = post.getPreferredAges();
        UserInfo authorInfo = UserInfo.from(post.getUser());

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
                preferredAges,
                photoUrls,
                tags,
                post.getViewCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                authorInfo,
                isOwner,
                hasApplied
        );
    }
}
