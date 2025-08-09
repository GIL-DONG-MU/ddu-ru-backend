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
        List<String> photoUrls,
        Integer viewCount
) {
    private static final int SUMMARY_MAX_LENGTH = 100;
    public static PostSummaryResponse from(Post post, JsonConverter jsonConverter) {

        String summaryContent = createSummaryContent(post.getContent());
        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                summaryContent,
                post.isRecruitOpen(),
                post.getDaysLeftForRecruitment(),
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                photoUrls,
                post.getViewCount()
        );
    }

    private static String createSummaryContent(String content) {
        if (content.length() > SUMMARY_MAX_LENGTH) {
            return content.substring(0, SUMMARY_MAX_LENGTH) + "...";
        }
        return content;
    }
}
