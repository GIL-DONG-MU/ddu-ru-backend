package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.PostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        PostStatus status,
        boolean isRecruitOpen,
        int daysLeft,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        Integer recruitCapacity,
        Integer recruitCount,
        LocalDate recruitDeadline,
        String preferredGender,
        String preferredAgeMin,
        String preferredAgeMax,
        Integer budgetMin,
        Integer budgetMax,
        List<String> photoUrls,
        List<String> tags,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserInfo author
) {
    public static PostDetailResponse from(Post post, JsonConverter jsonConverter) {
        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());
        UserInfo authorInfo = UserInfo.from(post.getUser());

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.isRecruitOpen(),
                post.getDaysLeftForRecruitment(),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getRecruitDeadline(),
                post.getPreferredGender() != null ? post.getPreferredGender().name() : null,
                post.getPreferredAgeMin() != null ? post.getPreferredAgeMin().name() : null,
                post.getPreferredAgeMax() != null ? post.getPreferredAgeMax().name() : null,
                post.getBudgetMin(),
                post.getBudgetMax(),
                photoUrls,
                tags,
                post.getViewCount(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                authorInfo
        );
    }
}
