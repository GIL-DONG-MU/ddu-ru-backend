package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.dto.UserInfo;

import java.time.LocalDate;
import java.util.List;

public record PostSummaryResponse(
        Long id,
        String title,
        String content,
        boolean isRecruitOpen,
        int daysLeft,
        int daysUntilTravelStart,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        Integer recruitCapacity,
        Integer recruitCount,
        String preferredGender,
        Integer budgetMin,
        Integer budgetMax,
        List<String> photoUrls,
        Integer viewCount,
        int likeCount,
        UserInfo author
) {
    public static PostSummaryResponse from(Post post, JsonConverter jsonConverter) {
        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());
        UserInfo authorInfo = UserInfo.from(post.getUser());

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.isRecruitOpen(),
                post.getDaysLeftForRecruitment(),
                post.getDaysUntilTravelStart(),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getPreferredGender().name(),
                post.getBudgetMin(),
                post.getBudgetMax(),
                photoUrls,
                post.getViewCount(),
                post.getLikeCount(),
                authorInfo
        );
    }
}
