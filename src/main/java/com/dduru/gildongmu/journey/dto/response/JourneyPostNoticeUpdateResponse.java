package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyPost;

public record JourneyPostNoticeUpdateResponse(
        Long journeyPostId,
        boolean isNotice
) {
    public static JourneyPostNoticeUpdateResponse from(JourneyPost journeyPost) {
        return new JourneyPostNoticeUpdateResponse(
                journeyPost.getId(),
                journeyPost.isNotice()
        );
    }
}
