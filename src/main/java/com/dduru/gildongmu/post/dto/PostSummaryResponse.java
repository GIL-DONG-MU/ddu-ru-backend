package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;

import java.time.LocalDate;
import java.util.List;

public record PostSummaryResponse(
        Long id,
        String title,
        String content,
        boolean isRecruitOpen,
        int daysLeft,
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
        int likeCount
) {
    private static final int SUMMARY_MAX_LENGTH = 100;
    public static PostSummaryResponse from(Post post, JsonConverter jsonConverter) {

        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.isRecruitOpen(),
                post.getDaysLeftForRecruitment(),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getPreferredGender() != null ? post.getPreferredGender().name() : "U",
                post.getBudgetMin(),
                post.getBudgetMax(),
                photoUrls,
                post.getViewCount(),
                post.getLikeCount()
        );
    }
}
