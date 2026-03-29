package com.dduru.gildongmu.admin.post.dto.response;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminPostDetailResponse(
        Long id,
        String title,
        String content,
        PostStatus status,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        Integer recruitCapacity,
        Integer recruitCount,
        LocalDate recruitDeadline,
        Gender preferredGender,
        boolean isAgeAny,
        Integer minAge,
        Integer maxAge,
        String photoUrl,
        List<String> tags,
        Long authorId,
        String authorName,
        LocalDateTime createdAt,
        boolean isDeleted,
        LocalDateTime deletedAt,
        Long deletedBy
) {
    public static AdminPostDetailResponse from(Post post, JsonConverter jsonConverter) {
        String photoUrl = post.getPhotoUrl();
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());
        return new AdminPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus(),
                post.getDestination().getCity(),
                post.getStartDate(),
                post.getEndDate(),
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                post.getRecruitDeadline(),
                post.getPreferredGender(),
                post.isAgeAny(),
                post.getMinAge(),
                post.getMaxAge(),
                photoUrl,
                tags,
                post.getUser().getId(),
                post.getUser().getName(),
                post.getCreatedAt(),
                post.isDeleted(),
                post.getDeletedAt(),
                post.getDeletedBy()
        );
    }
}
