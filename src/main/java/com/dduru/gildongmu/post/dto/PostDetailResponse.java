package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.PostStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String recruitmentStatus,
        Long daysLeft,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        List<String> photoUrls,
        Integer viewCount,
        List<String> tags,
        Integer recruitCapacity,
        Integer recruitCount,
        String budgetRange,
        String authorName,
        Long authorId,
        String preferredGender,
        String preferredAgeRange
) {
    public static PostDetailResponse from(Post post, JsonConverter jsonConverter) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), post.getRecruitDeadline()) + 1;
        if (daysLeft < 0) daysLeft = 0;

        String recruitmentStatus = determineRecruitmentStatus(post, daysLeft);

        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());
        List<String> tags = jsonConverter.convertJsonToList(post.getTags());

        String budgetRange = formatBudgetRange(post.getBudgetMin(), post.getBudgetMax());
        String preferredAgeRange = formatPreferredAgeRange(post.getPreferredAgeMin(), post.getPreferredAgeMax());

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                recruitmentStatus,
                daysLeft,
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                photoUrls,
                post.getViewCount(),
                tags,
                post.getRecruitCapacity(),
                post.getRecruitCount(),
                budgetRange,
                post.getUser().getName(),
                post.getUser().getId(),
                post.getPreferredGender().getKoreanName(),
                preferredAgeRange
        );
    }

    private static String determineRecruitmentStatus(Post post, long daysLeft) {
        if (post.getStatus() == PostStatus.OPEN &&
                daysLeft > 0 &&
                !post.isRecruitmentClosed() &&
                post.getRecruitCount() < post.getRecruitCapacity()) {
            return "모집중";
        } else {
            return "모집완료";
        }
    }

    private static String formatBudgetRange(Integer budgetMin, Integer budgetMax) {
        if (budgetMin == null && budgetMax == null) {
            return "예산 미정";
        }

        if (budgetMin != null && budgetMax != null) {
            return String.format("%,d원 ~ %,d원", budgetMin, budgetMax);
        }

        if (budgetMin != null) {
            return String.format("%,d원 이상", budgetMin);
        }

        return String.format("%,d원 이하", budgetMax);
    }

    private static String formatPreferredAgeRange(AgeRange ageMin, AgeRange ageMax) {
        if (ageMin == null && ageMax == null) {
            return "연령 무관";
        }

        if (ageMin != null && ageMax != null) {
            if (ageMin == ageMax) {
                return ageMin.getDescription();
            }
            return ageMin.getDescription() + " ~ " + ageMax.getDescription();
        }

        if (ageMin != null) {
            return ageMin.getDescription() + " 이상";
        }

        return ageMax.getDescription() + " 이하";
    }
}
