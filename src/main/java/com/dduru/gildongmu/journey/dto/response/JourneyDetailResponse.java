package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;

import java.time.LocalDate;
import java.util.List;

public record JourneyDetailResponse(
        Long journeyId,
        Long groupRoomId,
        String title,
        String photoUrl,
        LocalDate startDate,
        LocalDate endDate,
        Integer recruitCount,
        String recruitDeadlineDDay,
        Integer recruitCapacity,
        boolean isOwner,
        List<JourneyMemberInfo> members,
        List<PinnedNoticeInfo> pinnedNotices
) {
    public static JourneyDetailResponse from(
            Journey journey,
            Long groupRoomId,
            PostDetailResponse postDetail,
            List<JourneyMemberInfo> members,
            List<PinnedNoticeInfo> pinnedNotices
    ) {
        return new JourneyDetailResponse(
                journey.getId(),
                groupRoomId,
                journey.getTitle(),
                journey.getPhotoUrl(),
                postDetail.startDate(),
                postDetail.endDate(),
                postDetail.recruitCount(),
                postDetail.recruitDeadlineDDay(),
                postDetail.recruitCapacity(),
                postDetail.isOwner(),
                members,
                pinnedNotices
        );
    }
}
