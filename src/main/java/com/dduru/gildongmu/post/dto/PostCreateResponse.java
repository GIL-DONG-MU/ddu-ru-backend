package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.Destination;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PostCreateResponse(
        Long id,
        UserInfo user,
        DestinationInfo destination,
        String title,
        String content,
        LocalDate startDate,
        LocalDate endDate,
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
        LocalDateTime modifiedAt,
        boolean isRecruitmentClosed,
        boolean isTravelStarted,
        boolean isTravelEnded
) {
    public static PostCreateResponse of(Post post, User user, Destination destination, List<String> photoUrls, List<String> tags) {
        return new PostCreateResponse(
                post.getId(),
                UserInfo.from(user),
                DestinationInfo.from(destination),
                post.getTitle(),
                post.getContent(),
                post.getStartDate(),
                post.getEndDate(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getRecruitDeadline(),
                post.getPreferredGender().name(),
                post.getPreferredAgeMin() != null ? post.getPreferredAgeMin().name() : null,
                post.getPreferredAgeMax() != null ? post.getPreferredAgeMax().name() : null,
                post.getBudgetMin(),
                post.getBudgetMax(),
                photoUrls,
                tags,
                post.getViewCount(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.isRecruitmentClosed(),
                post.isTravelStarted(),
                post.isTravelEnded()
        );
    }
}
