package com.dduru.gildongmu.participation.dto.query;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;

import java.time.LocalDateTime;

public record ParticipationRetrieveQueryResult(
        Long participationId,
        Long userId,
        String userName,
        ProfileImageType profileImageType,
        String uploadedImageUrl,
        String avatarImageUrl,
        String message,
        ParticipationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime contactedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        Long postId,
        String postTitle
) {
}
