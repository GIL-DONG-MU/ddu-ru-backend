package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JourneyPostResponse(
        Long journeyPostId,
        ParticipantInfo author,
        String content,
        List<String> imageUrls,
        boolean isNotice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isAuthor,
        long commentCount
) {
    public static JourneyPostResponse from(
            JourneyPost journeyPost,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver,
            long commentCount,
            LocalDate today
    ) {
        return new JourneyPostResponse(
                journeyPost.getId(),
                ParticipantInfo.from(
                        journeyPost.getAuthor(),
                        hostUserId.equals(journeyPost.getAuthor().getId()),
                        profileImageResolver,
                        today
                ),
                journeyPost.getContent(),
                journeyPost.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList(),
                journeyPost.isNotice(),
                journeyPost.getCreatedAt(),
                journeyPost.getModifiedAt(),
                journeyPost.isAuthor(currentUserId),
                commentCount
        );
    }
}
