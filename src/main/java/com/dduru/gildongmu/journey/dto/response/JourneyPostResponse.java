package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;

import java.time.LocalDateTime;
import java.util.List;

public record JourneyPostResponse(
        Long journeyPostId,
        ParticipantInfo author,
        String title,
        String content,
        String imageUrl,
        boolean isNotice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isAuthor,
        long commentCount,
        boolean hasMoreComments,
        List<JourneyPostCommentResponse> previewComments
) {
    public static JourneyPostResponse from(
            JourneyPost journeyPost,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver
    ) {
        return from(journeyPost, currentUserId, hostUserId, profileImageResolver, 0L, List.of());
    }

    public static JourneyPostResponse from(
            JourneyPost journeyPost,
            Long currentUserId,
            Long hostUserId,
            ProfileImageResolver profileImageResolver,
            long commentCount,
            List<JourneyPostComment> previewComments
    ) {
        List<JourneyPostCommentResponse> previewCommentResponses = previewComments.stream()
                .map(comment -> JourneyPostCommentResponse.from(comment, currentUserId, hostUserId, profileImageResolver))
                .toList();

        return new JourneyPostResponse(
                journeyPost.getId(),
                ParticipantInfo.from(
                        journeyPost.getAuthor(),
                        hostUserId != null && hostUserId.equals(journeyPost.getAuthor().getId()),
                        profileImageResolver
                ),
                journeyPost.getTitle(),
                journeyPost.getContent(),
                journeyPost.getImageUrl(),
                journeyPost.isNotice(),
                journeyPost.getCreatedAt(),
                journeyPost.getModifiedAt(),
                journeyPost.isAuthor(currentUserId),
                commentCount,
                commentCount > previewCommentResponses.size(),
                previewCommentResponses
        );
    }
}
