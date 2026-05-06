package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.dto.response.ParticipantInfo;
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
        List<ParticipantInfo> participants
) {
    public static JourneyDetailResponse from(Journey journey, Long groupRoomId, PostDetailResponse postDetail) {
        // 상세 상단에 필요한 최소 정보만 남기고, journey와 post의 책임 경계가 드러나도록 조립한다.
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
                postDetail.participants()
        );
    }
}
