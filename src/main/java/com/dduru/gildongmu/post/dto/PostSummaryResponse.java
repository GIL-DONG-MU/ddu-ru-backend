package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.PostStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record PostSummaryResponse(
        Long id,
        String title,
        String content,
        String recruitmentStatus,
        Long daysLeft,
        LocalDate startDate,
        LocalDate endDate,
        String destination,
        List<String> photoUrls,
        Integer viewCount
) {
    public static PostSummaryResponse from(Post post, JsonConverter jsonConverter) {
        long daysLeft= ChronoUnit.DAYS.between(LocalDate.now(), post.getRecruitDeadline())+ 1;
        if(daysLeft<0) daysLeft = 0;

        String recruitmentStatus = determineRecruitmentStatus(post, daysLeft);

        String summaryContent;
        if(post.getContent().length() > 100){
            summaryContent = post.getContent().substring(0, 100) + "...";
        }else{
            summaryContent = post.getContent();
        }

        List<String> photoUrls = jsonConverter.convertJsonToList(post.getPhotoUrls());

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                summaryContent,
                recruitmentStatus,
                daysLeft,
                post.getStartDate(),
                post.getEndDate(),
                post.getDestination().getCity(),
                photoUrls,
                post.getViewCount()
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
}
