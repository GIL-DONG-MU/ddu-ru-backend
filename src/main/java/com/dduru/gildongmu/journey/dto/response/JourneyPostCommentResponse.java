package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDateTime;

public record JourneyPostCommentResponse(
        Long commentId,
        Long journeyPostId,
        ParticipantInfo author,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isAuthor
) {
    public static JourneyPostCommentResponse from(
            JourneyPostComment comment,
            Long journeyPostId,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        Long authorUserId = comment.getAuthor().getId();
        return new JourneyPostCommentResponse(
                comment.getId(),
                journeyPostId,
                ParticipantInfo.from(
                        comment.getAuthor(),
                        hostUserId.equals(authorUserId),
                        profileImageResolver
                ),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.isAuthor(currentUserId)
        );
    }
}
