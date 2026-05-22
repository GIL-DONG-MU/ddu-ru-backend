package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDateTime;

public record JourneyPostResponse(
        Long journeyPostId,
        ParticipantInfo author,
        String content,
        String imageUrl,
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
            long commentCount
    ) {
        return new JourneyPostResponse(
                journeyPost.getId(),
                ParticipantInfo.from(
                        journeyPost.getAuthor(),
                        hostUserId != null && hostUserId.equals(journeyPost.getAuthor().getId()),
                        profileImageResolver
                ),
                journeyPost.getContent(),
                journeyPost.getImageUrl(),
                journeyPost.isNotice(),
                journeyPost.getCreatedAt(),
                journeyPost.getModifiedAt(),
                journeyPost.isAuthor(currentUserId),
                commentCount
        );
    }
}
